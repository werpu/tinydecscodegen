package supportive.fs.tn;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import indexes.ModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.*;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;
import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.literalEquals;

public class TNUIRoutesFileContext extends TNRoutesFileContext {

    private static final Logger log = Logger.getInstance(TNUIRoutesFileContext.class);
    public static final Object[] STATE_PROVIDER_VAR = {JS_EXPRESSION_STATEMENT, JS_REFERENCE_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, NAME_EQ("$stateProvider")};

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
                .queryContent(PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("'state'"))
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

    //trying to reimplement
    public List<PsiRouteContext> parse(PsiElementContext argumentsList) {

        //first part either name or call or map
        try {
            Optional<PsiElementContext> routeName = argumentsList.queryContent(JS_LITERAL_EXPRESSION, ">" + PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
            Optional<PsiElementContext> parmsCall = argumentsList.queryContent(JS_REFERENCE_EXPRESSION, TEXT_EQ("MetaData.routeData")).findFirst();
            Optional<PsiElementContext> controller = argumentsList.queryContent(JS_CALL_EXPRESSION, JS_ARGUMENTS_LIST, JS_REFERENCE_EXPRESSION).findFirst();
            Optional<PsiElementContext> parmsMap = argumentsList.queryContent(JS_OBJECT_LITERAL_EXPRESSION).findFirst();
            List<PsiRouteContext> target = resolveParms(argumentsList, routeName, parmsCall, controller, parmsMap);
            if (target != null) return target;

            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error(e);
            return Collections.emptyList();
        }

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


        if (parmsMap.isPresent() && routeName.isPresent() && controller.isPresent() && parmsCall.isPresent() && routeName.get().getTextOffset() < parmsCall.get().getTextOffset()) {
            //route name and parms call
            Optional<IntellijFileContext> pageController = resolveController(controller);
            Route target = new Route(routeName.get().getText(), "", controller.get().getText(), this.getClass());
            if (pageController.isPresent()) {
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
             *          {
             *               name: "myState.state4",
             *               url: "/myState"
             *          }
             * )
             */
            Optional<IntellijFileContext> pageController = controller.isPresent() ? resolveController(controller) : Optional.empty();

            Route rt = new Route("", "", controller.isPresent() ? controller.get().getText() : "", this.getClass());

            rt.setComponentPath(pageController.isPresent() ? pageController.get().getVirtualFile().getPath() : "");
            Optional<PsiElementContext> views = Optional.empty();
            if(parmsMap.isPresent()) {
                resolveParamsMap(rt, parmsMap.get());
                views = resolveObjectProp(parmsMap.get(), "views");

            }
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
             *        {
             *             name: "myState.state4",
             *             url: "/myState"
             *        }
             *    ),
             */

            try {
                Optional<PsiElementContext> propKey = findFirstPropKey(prop);
                Optional<PsiElementContext> newParamsMap = prop.queryContent(JS_OBJECT_LITERAL_EXPRESSION).findFirst();

                final String viewName = propKey.get().getText();
                Optional<PsiElementContext> callExpr = prop.queryContent(">" + JS_CALL_EXPRESSION).findFirst();
                Optional<PsiElementContext> controller2 = callExpr.isPresent() ? callExpr.get().queryContent(">" + JS_ARGUMENTS_LIST, JS_REFERENCE_EXPRESSION).findFirst() : Optional.empty();
                Optional<PsiElementContext> paramsMap2 = callExpr.isPresent() ? callExpr.get().queryContent(">" + JS_ARGUMENTS_LIST, JS_OBJECT_LITERAL_EXPRESSION).findFirst(): Optional.empty();
                List<PsiRouteContext> rets = resolveParms(routeCall, routeName, callExpr, controller2.isPresent() ? controller2 : controller,newParamsMap.isPresent() ? newParamsMap : paramsMap2);
                rets.stream().forEach(el -> {
                    el.getRoute().setViewName(viewName);
                    if(Strings.isNullOrEmpty(el.getRoute().getRouteKey()) && routeName.isPresent()) {
                        el.getRoute().setRouteKey(routeName.get().getText());
                    }
                    if(Strings.isNullOrEmpty(el.getRoute().getComponentPath()) && controller.isPresent()) {
                        Optional<IntellijFileContext> intellijFileContext = resolveController(controller);
                        el.getRoute().setComponentPath(intellijFileContext.isPresent() ? intellijFileContext.get().getVirtualFile().getPath(): "");
                    }
                });
                return rets.stream();
            } catch (RuntimeException ex) {
                log.error(ex);
                return Collections.<PsiRouteContext>emptyList().stream();
            }
        }).collect(Collectors.toList());

    }

    @NotNull
    public Optional<PsiElementContext> findFirstPropKey(PsiElementContext prop) {
        Optional<PsiElementContext> el1 =  prop.queryContent(">" + PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        Optional<PsiElementContext> el2=  prop.queryContent(">" + JS_PROPERTY, PSI_ELEMENT_JS_IDENTIFIER).findFirst();

        Optional<PsiElementContext> propKey = null;
        if(el1.isPresent() && !el2.isPresent()) {
            propKey = el1;
        } else if(el2.isPresent() && !el1.isPresent()) {
            propKey = el2;
        } else {
            propKey = el1.get().getTextOffset() < el1.get().getTextOffset() ? el1 : el2;
        }
        return propKey;
    }

    @NotNull
    public Optional<IntellijFileContext> resolveController(Optional<PsiElementContext> controller) {
        String controllerName = controller.get().getText();
        Optional<PsiElement> importStr = findImportString(controllerName);
        if (!importStr.isPresent()) {
            return findExternalImport(controllerName);
        } else {
            return Optional.ofNullable(this.relative(importStr.get()));
        }
    }

    @Nullable
    public Optional<IntellijFileContext> findExternalImport(String controllerName) {
        //TODO fallback into a global search
        List<IntellijFileContext> module = ModuleIndex.getAllAffectedFiles(getProject(), getAngularRoot().get()).stream()
                .filter(psiFile -> {

                    if (psiFile == null || psiFile.getVirtualFile() == null ||
                            psiFile.getVirtualFile().getExtension() == null ||
                            psiFile.getContainingFile() == null ||
                            psiFile.getContainingFile().getText() == null ||
                            psiFile.getVirtualFile().getPath().contains("node_modules") ||
                            !psiFile.getVirtualFile().getExtension().equalsIgnoreCase("ts")) {
                        return false;
                    }
                    String text = psiFile.getContainingFile().getText();
                    return (text.contains(NG_MODULE) ||
                            text.trim().contains(".module(")) && (
                            text.trim().contains(".controller(\"" + controllerName + "\"") ||
                                    text.trim().contains(".controller('" + controllerName + "'") || text.trim().contains(".controller(\"" + controllerName + "\")") ||
                                    text.trim().contains(".component(\"" + controllerName + "\"") ||
                                    text.trim().contains(".component('" + controllerName + "'")

                    );
                }).map(el -> new IntellijFileContext(getProject(), el))
                .collect(Collectors.toList());

        if (!module.isEmpty()) {
            Optional<IntellijFileContext> controllerDef = module.stream().map(fc -> {
                Optional<PsiElement> importStr2 = queryController(fc)
                        .map(el -> findMethodCallStart(el, JS_CALL_EXPRESSION))
                        .filter(el2 -> hasControllerName(controllerName, el2))
                        .flatMap(el -> getImportStrinStream(fc, el)).findFirst().orElse(Optional.empty());
                if(importStr2.isPresent()) {
                    return fc.relative(importStr2.get());
                }
                return null;

            }).filter(e -> e != null).findFirst();
            if (controllerDef.isPresent()) {
                return controllerDef;
            }
            return module.stream().map(fc -> {
                Optional<PsiElement> importStr2 = queryComponent(fc)
                        .map(el -> findMethodCallStart(el, JS_CALL_EXPRESSION))
                        .filter(el2 -> hasControllerName(controllerName, el2))
                        .flatMap(el -> getImportStrinStream(fc, el)).findFirst().orElse(Optional.empty());
                if(importStr2.isPresent()) {
                    return fc.relative(importStr2.get());
                }
                return null;

            }).filter(e -> e != null).findFirst();
        }
        return Optional.empty();
    }

    public Stream<Optional<PsiElement>> getImportStrinStream(IntellijFileContext fc, Optional<PsiElementContext> el) {
        return Collections.singletonList(getImportString(fc, el)).stream();
    }

    public Optional<PsiElement> getImportString(IntellijFileContext fc, Optional<PsiElementContext> el) {
        //controllerNam
        PsiElementContext ctrl = el.get()
                .queryContent(JS_REFERENCE_EXPRESSION)
                .reduce((el1, el2) -> el2).get();
        return findImportString(new TypescriptFileContext(fc), ctrl.getText());
    }

    public boolean hasControllerName(String controllerName, Optional<PsiElementContext> el2) {
        return el2.isPresent() && el2.get()
                .queryContent(PSI_ELEMENT_JS_STRING_LITERAL, TEXT_EQ(controllerName))
                .findFirst().isPresent();
    }

    public Stream<PsiElementContext> queryController(IntellijFileContext fc) {
        return fc.queryContent(PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("controller"));
    }

    public Stream<PsiElementContext> queryComponent(IntellijFileContext fc) {
        return fc.queryContent(PSI_ELEMENT_JS_IDENTIFIER, TEXT_EQ("component"));
    }

    public static Optional<PsiElementContext> findMethodCallStart(PsiElementContext el, String jsCallExpression) {
        return el.walkParent(el2 -> literalEquals(el2.toString(), jsCallExpression));
    }


    void resolveParamsMap(Route target, PsiElementContext paramsMap) {

        Optional<PsiElementContext> name = resolveStringProp(paramsMap, "name");
        Optional<PsiElementContext> url = resolveStringProp(paramsMap, "url");
        Optional<PsiElementContext> controller = resolveStringProp(paramsMap, "controller");

        if (controller.isPresent()) {
            Optional<IntellijFileContext> controllerFile = resolveController(controller);

            target.setComponent(controller.get().getText());
            if (controllerFile.isPresent()) {
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
        return paramsMap.queryContent(JS_PROPERTY, NAME_EQ(prop), targetType).findFirst();
    }


}
