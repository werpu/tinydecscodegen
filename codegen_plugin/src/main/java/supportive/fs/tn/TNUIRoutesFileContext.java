package supportive.fs.tn;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.PsiElementContext;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.common.Route;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.literalEquals;

public class TNUIRoutesFileContext extends TNRoutesFileContext {

    private static final Logger log = Logger.getInstance(TNUIRoutesFileContext.class);

    public TNUIRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
        init();
    }

    public TNUIRoutesFileContext(AnActionEvent event) {
        super(event);
        init();
    }

    public TNUIRoutesFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        init();
    }

    public TNUIRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
        init();
    }

    /**
     * find the state provider root elements
     *
     * @param item
     * @return
     */
    private static Optional<PsiElementContext> findStateProvidersCalls(PsiElementContext item) {
        //JS_EXPRESSION_STATEMENT == $stateProvider.state(....).state
        //      JS_REFERENCE_EXPRESSION == $stateProvider.state
        //          PSI_ELEMENT_JS_IDENTIFIER == $stateProvider
        Optional<PsiElementContext> found = item.queryContent(JS_EXPRESSION_STATEMENT, JS_REFERENCE_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, "NAME:($stateProvider)")
                //back to stateprivider call
                .filter(item3 -> item3.walkParent(item2 -> literalEquals(item2.toString(), JS_EXPRESSION_STATEMENT) && item2.getText().startsWith("$stateProvider")).isPresent())
                //find the topmost call
                .map(item4 -> item4.walkParent(item5 -> literalEquals(item5.toString(), JS_EXPRESSION_STATEMENT)).get()).reduce((el1, el2) -> el2);

        return found;
    }


    protected void init() {

        constructors = this.queryContent(">" + TYPE_SCRIPT_FUNC, p_isConstructor(), p_isStateProviderPresent()
        ).collect(Collectors.toList());
    }

    @Override
    public void addRoute(Route routeData) {
        routeData.setComponent(appendImport(routeData.getComponent(), routeData.getComponentPath()));

        int cnt = 1;
        String origUrl = routeData.getUrl();

        while (isUrlInUse(routeData)) {
            routeData.setUrl(origUrl + "_" + cnt);
            cnt++;
        }
        //PsiElement routesDeclaration = getRoutesDeclaration().stream().reduce((el1, el2) -> el2).get();

        PsiElementContext constr = constructors.stream().filter(p_isStateProviderPresent()).findFirst().get();
        String stateProviderName = getStateOrRouteProviderName(constr);

        List<PsiElementContext> stateParams = getRouteParams(constr, stateProviderName);
        PsiElement routesDeclaration = stateParams.get(stateParams.size() - 1).getElement();
        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(routesDeclaration.getTextOffset() + routesDeclaration.getTextLength() + 1), routeData.toStringTnNg1()));
        //addNavVar(routeData.getRouteVarName());
    }


    @Override
    public List<PsiRouteContext> getRoutes() {
        boolean stateProvider = constructors.stream().filter(p_isStateProviderPresent()).findFirst().isPresent();


        if (stateProvider) {
            return mapStates();
        }
        return Collections.emptyList();
    }


    /**
     * maps the source states into their respective route context objects
     *
     * @return
     */
    private List<PsiRouteContext> mapStates() {
        PsiElementContext constr = constructors.stream().filter(p_isStateProviderPresent()).findFirst().get();
        String stateProviderName = getStateOrRouteProviderName(constr);

        List<PsiElementContext> stateParams = getRouteParams(constr, stateProviderName);
        return stateParams.stream()
                .flatMap(el -> parse(el).stream())
                .collect(Collectors.toList());


    }

    /**
     * just gets the list of route calls (not the subviews... needed for insert)
     *
     * @return
     */
    List<PsiElement> getRoutesDeclaration() {

        return constructors.stream().map(//we search for the last call into $stateProvider.when
                TNUIRoutesFileContext::findStateProvidersCalls)
                .filter(item -> item.isPresent())
                .map(item -> item.get().getElement())
                .collect(Collectors.toList());

    }


    /**
     * fetches the route params
     * <p>
     * aka
     * <p>
     * $stateProvider.state(
     * MetaData.routeData(View1,
     * {
     * name: "myState",
     * url:"/myState"
     * }
     * )
     * )
     * <p>
     * returns (
     * MetaData.routeData(View1,
     * {
     * name: "myState",
     * url:"/myState"
     * }
     * )
     * )
     *
     * @param constructor
     * @param routeProviderName
     * @return
     */
    @Override
    public List<PsiElementContext> getRouteParams(PsiElementContext constructor, String routeProviderName) {

        return constructor
                //.state(...
                .queryContent(PSI_ELEMENT_JS_IDENTIFIER, "TEXT:('state')")
                //$stateProvider.state
                .map(item -> item.walkParent(el -> {
                    return literalEquals(el.toString(), JS_EXPRESSION_STATEMENT);
                }))
                .filter(item -> item.isPresent())
                //arguments
                //(....
                .flatMap(item -> item.get().queryContent(JS_ARGUMENTS_LIST))
                //state..
                .filter(item -> {
                    Optional<PsiElement> prev = elVis(item, "element", "prevSibling");
                    return prev.isPresent() && prev.get().getText().endsWith(".state");
                })
                .distinct()
                .collect(Collectors.toList());

    }

    /**
     * returns the subelement hosting the route meta data
     * {
     * name: "myState",
     * url:"/myState"
     * }
     */
    PsiElementContext getRouteMeta(PsiElementContext routeParam) {
        return routeParam.queryContent(JS_ARGUMENTS_LIST, JS_OBJECT_LITERAL_EXPRESSION)
                .findFirst().get();
    }


    //trying to reimplement
    public List<PsiRouteContext> parse(PsiElementContext argumentsList) {
        //first part either name or call or map


        Optional<PsiElementContext> routeName = argumentsList.queryContent(JS_LITERAL_EXPRESSION, ">" + PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        Optional<PsiElementContext> parmsCall = argumentsList.queryContent(JS_REFERENCE_EXPRESSION, "TEXT:(MetaData.routeData)").findFirst();
        Optional<PsiElementContext> controller = argumentsList.queryContent(JS_CALL_EXPRESSION, JS_ARGUMENTS_LIST, JS_REFERENCE_EXPRESSION).findFirst();
        Optional<PsiElementContext> parmsMap = argumentsList.queryContent(JS_OBJECT_LITERAL_EXPRESSION).findFirst();
        List<PsiRouteContext> target = resolveParms(argumentsList, routeName, parmsCall, controller, parmsMap);
        if (target != null) return target;

        return Collections.emptyList();
        //if(routeName.isPresent() && routeName.get().getElement().getTextOffset() < )

    }

    @Nullable
    public List<PsiRouteContext> resolveParms(PsiElementContext routeCall, Optional<PsiElementContext> routeName, Optional<PsiElementContext> parmsCall, Optional<PsiElementContext> controller, Optional<PsiElementContext> parmsMap) {
        /**
         * standard case
         * state("myState.state2",
         *             MetaData.routeData(View2,
         *                 {
         *                     url: "/myState"
         *                 }
         *             )
         *         )
         *
         */


        if (routeName.isPresent() && controller.isPresent() && parmsCall.isPresent() && routeName.get().getTextOffset() < parmsCall.get().getTextOffset()) {
            //route name and parms call
            Optional<IntellijFileContext> pageController = resolveController(controller);
            Route target = new Route(routeName.get().getText(), "",controller.get().getText(), this.getClass());
            if(pageController.isPresent()) {
                target.setComponentPath(pageController.get().getVirtualFile().getPath());
                //TODO error log to identify the issue
            }

            Optional<PsiElementContext> views = resolveObjectProp(parmsMap.get(), "views");
            if (!views.isPresent()) {
                resolveParamsMap(target, parmsMap.get());
                return Collections.singletonList(new PsiRouteContext(routeCall.getElement(), target));
            } else {
                //map with views or map with views
                return resolveViews(routeCall, routeName, controller, views);
            }

        } else if (routeName.isPresent() && parmsMap.isPresent() && routeName.get().getTextOffset() < parmsMap.get().getTextOffset()) {
            //no controller present

            //map without views or map with views
            Optional<PsiElementContext> views = resolveObjectProp(parmsMap.get(), "views");
            if (!views.isPresent()) {
                Route target = new Route(routeName.get().getText(), "", "", this.getClass());
                resolveParamsMap(target, parmsMap.get());
                return Collections.singletonList(new PsiRouteContext(routeCall.getElement(), target));
            } else {
                //map with views or map with views
                return resolveViews(routeCall, routeName, controller, views);
            }
        } else {
            //case 3 map only
            //TODO
            /**
             * (MetaData.routeData(View2,
             *              *                     {
             *              *                         name: "myState.state4",
             *              *                         url: "/myState"
             *              *                     }
             *              *                 )
             */
            Optional<IntellijFileContext> pageController = controller.isPresent() ? resolveController(controller) : Optional.empty();

            Route rt = new Route("", "", controller.isPresent() ? controller.get().getText() : "", this.getClass());

            rt.setComponentPath(pageController.isPresent() ? pageController.get().getVirtualFile().getPath() : null);
            resolveParamsMap(rt, parmsMap.get());
            Optional<PsiElementContext> views = resolveObjectProp(parmsMap.get(), "views");
            if (!views.isPresent()) {
                return Collections.singletonList(new PsiRouteContext(routeCall.getElement(), rt));
            } else {
                Optional<PsiElementContext> controller2 = resolveObjectProp(parmsMap.get(), "controller");
                Optional<PsiElementContext> rtName = resolveStringProp(parmsMap.get(), "name");

                return resolveViews(routeCall, rtName, controller2, views);
            }


        }

    }

    public List<PsiRouteContext> resolveViews(PsiElementContext routeCall, Optional<PsiElementContext> routeName, Optional<PsiElementContext> controller, Optional<PsiElementContext> views) {
        return views.get().queryContent(">" + JS_PROPERTY).flatMap(prop -> {
            /**
             *   "viewMain": MetaData.routeData(View2,
             *                     {
             *                         name: "myState.state4",
             *                         url: "/myState"
             *                     }
             *                 ),
             */
            final String viewName = prop.queryContent(">" + PSI_ELEMENT_JS_STRING_LITERAL).findFirst().get().getText();
            Optional<PsiElementContext> callExpr = prop.queryContent(">" + JS_CALL_EXPRESSION).findFirst();
            Optional<PsiElementContext> controller2 = callExpr.get().queryContent(">" + JS_ARGUMENTS_LIST, JS_REFERENCE_EXPRESSION).findFirst();
            Optional<PsiElementContext> paramsMap2 = callExpr.get().queryContent(">" + JS_ARGUMENTS_LIST, JS_OBJECT_LITERAL_EXPRESSION).findFirst();
            List<PsiRouteContext> rets = resolveParms(routeCall, routeName, callExpr, controller2.isPresent() ? controller2 : controller, paramsMap2);
            rets.stream().forEach(el -> el.getRoute().setViewName(viewName));
            return rets.stream();
        }).collect(Collectors.toList());
    }

    @NotNull
    public Optional<IntellijFileContext> resolveController(Optional<PsiElementContext> controller) {
        Optional<PsiElement> importStr = findImportString(controller.get().getText());
        if(!importStr.isPresent()) {
            log.warn("no import found on controller"+controller.get().getText());
            return Optional.empty();
        }
        return  Optional.ofNullable(new IntellijFileContext(getProject(), getVirtualFile().getParent().findFileByRelativePath(StringUtils.stripQuotes(importStr.get().getText()) + ".ts")));
    }

    void resolveParamsMap(Route target, PsiElementContext paramsMap) {

        Optional<PsiElementContext> name = resolveStringProp(paramsMap, "name");
        Optional<PsiElementContext> url = resolveStringProp(paramsMap, "url");
        Optional<PsiElementContext> controller = resolveStringProp(paramsMap, "controller");

        if (controller.isPresent()) {
            Optional<IntellijFileContext> controllerFile = resolveController(controller);

            target.setComponent(controller.get().getText());
            if(controllerFile.isPresent()) {
                target.setComponentPath(controllerFile.get().getVirtualFile().getPath());
            }

        }

        if (name.isPresent()) {
            target.setRouteKey(name.get().getText());
        }
        if (url.isPresent()) {
            target.setUrl(url.get().getText());
        }
    }

    private Optional<PsiElementContext> resolveStringProp(PsiElementContext paramsMap, String prop) {
        return resolveProp(paramsMap, prop, PSI_ELEMENT_JS_STRING_LITERAL);
    }

    private Optional<PsiElementContext> resolveObjectProp(PsiElementContext paramsMap, String prop) {
        return resolveProp(paramsMap, prop, JS_OBJECT_LITERAL_EXPRESSION);
    }

    private Optional<PsiElementContext> resolveProp(PsiElementContext paramsMap, String prop, String targetType) {
        return paramsMap.queryContent(JS_PROPERTY, "NAME:(" + prop + ")", targetType).findFirst();
    }


}
