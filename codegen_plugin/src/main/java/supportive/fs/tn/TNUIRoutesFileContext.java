package supportive.fs.tn;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.bouncycastle.asn1.cms.MetaData;
import org.jetbrains.annotations.NotNull;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.literalEquals;

public class TNUIRoutesFileContext extends TNRoutesFileContext {

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

    private static Optional<PsiElementContext> findStateProvidersCalls(PsiElementContext item) {
        Optional<PsiElementContext> found = item.queryContent(JS_EXPRESSION_STATEMENT, JS_REFERENCE_EXPRESSION, PSI_ELEMENT_JS_IDENTIFIER, "NAME:($stateProvider)")
                .filter(item3 -> item3.walkParent(item2 -> literalEquals(item2.toString(), JS_EXPRESSION_STATEMENT) && item2.getText().startsWith("$stateProvider")).isPresent())
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
        PsiElement routesDeclaration = getRoutesDeclaration().stream().reduce((el1, el2) -> el2).get();

        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(routesDeclaration.getTextOffset()+routesDeclaration.getTextLength()+1), routeData.toStringNg1()));
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
     * @return
     */
    private List<PsiRouteContext> mapStates() {
        PsiElementContext constr = constructors.stream().filter(p_isStateProviderPresent()).findFirst().get();
        String stateProviderName = getStateOrRouteProviderName(constr);
        List<PsiElementContext> stateParams = getRouteParams(constr, stateProviderName);

        return stateParams.stream()
                .distinct()
                .flatMap(resolveArgs())
                .filter(item -> item.isPresent())
                .map(item -> item.get())
                .collect(Collectors.toList());
    }

    List<PsiElement> getRoutesDeclaration() {

        return constructors.stream().map(//we search for the last call into $stateProvider.when
                TNUIRoutesFileContext::findStateProvidersCalls)
                .filter(item -> item.isPresent())
                .map(item -> item.get().getElement())
                .collect(Collectors.toList());

    }


    /**
     * resolves the incoming arguments list accordingly
     *
     * we are different here, instead of a pure list/map
     * we have
     * (Controller, {argumentsMap})
     *
     * @return
     */
    @NotNull
    public Function<PsiElementContext, Stream<Optional<PsiRouteContext>>> resolveArgs() {
        return (PsiElementContext call) -> {//$provider.when
            final PsiElementContext call2 = call.queryContent(JS_CALL_EXPRESSION).findFirst().get();
            Stream<PsiElementContext> params = call.queryContent(JS_ARGUMENTS_LIST);


            List<PsiElementContext> args = call.queryContent(JS_CALL_EXPRESSION, JS_ARGUMENTS_LIST).collect(Collectors.toList());
            return params.map(arg -> mapArg(call2, arg));
        };
    }

    public Optional<String> resolveJsVar(PsiElementContext call) {
        //$routeProvider.when("/view1", MetaData.routeData(View1));
        return call.findPsiElements(psiElement -> psiElement.getText().startsWith("MetaData.routeData")).stream()
                .map(item -> item.queryContent(JS_ARGUMENTS_LIST,PSI_ELEMENT_JS_IDENTIFIER, P_FIRST).findFirst().get())
                .map(item -> item.getText()).findFirst();

    }



    /**
     * call MetaData.routeData
     * args (view,1{
     *             name: "myState",
     *             url:"/myState"
     *         })
     *
     * @param call
     * @param arg
     * @return
     */
    public Optional<PsiRouteContext> mapArg(PsiElementContext call, PsiElementContext arg) {

        Optional<String> classIdentifier = resolveJsVar(call);
        Optional<PsiElement> importPath = findImportString(classIdentifier.get()); //todo else handling
        if (!importPath.isPresent()) {
            return Optional.empty();
        }



        Optional<PsiElementContext> oUrl = arg.queryContent(JS_PROPERTY, "NAME:(url)",PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        Optional<PsiElementContext> oName = arg.queryContent(JS_PROPERTY, "NAME:(name)",PSI_ELEMENT_JS_STRING_LITERAL).findFirst();


        //load class file and fetch the missing meta info from there (namy only)

        IntellijFileContext pageController = new IntellijFileContext(getProject(), getVirtualFile().getParent().findFileByRelativePath(StringUtils.stripQuotes(importPath.get().getText()) + ".ts"));

        Optional<PsiElementContext> controllerDef = pageController.queryContent(JS_ES_6_DECORATOR, "TEXT*:(@Controller)", JS_PROPERTY, "NAME:(name)", PSI_ELEMENT_JS_STRING_LITERAL).findFirst();

        if (!controllerDef.isPresent()) {
            controllerDef = pageController.queryContent(JS_ES_6_DECORATOR, "TEXT*:(@Component)", JS_PROPERTY, "NAME:(name)", PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        }
        if (!controllerDef.isPresent()) {
            controllerDef = pageController.queryContent(JS_ES_6_DECORATOR, "TEXT*:(@Component)", JS_PROPERTY, "NAME:(selector)", PSI_ELEMENT_JS_STRING_LITERAL).findFirst();
        }

        String name = "";
        if (controllerDef.isPresent()) {
            name = StringUtils.stripQuotes(controllerDef.get().getText());
        }

        //now we have resolved the name, we have enough data to build our routes object


        String url = (oUrl.isPresent()) ? oUrl.get().getText() : "";
        String component = classIdentifier.orElse("No Class");
        Route route = new Route(oName.isPresent() ? oName.get().getText() : name, url, component, "", pageController.getVirtualFile().getPath(), this.getClass());

        return Optional.of(new PsiRouteContext(call.getElement(), route));
    }



    /**
     * fetches the route params
     *
     * aka
     *
     *$stateProvider.state(
     *     MetaData.routeData(View1,
     *         {
     *             name: "myState",
     *             url:"/myState"
     *         }
     *     )
     * )
     *
     * returns (
     *          MetaData.routeData(View1,
     *               {
     *                   name: "myState",
     *                   url:"/myState"
     *               }
     *           )
     *       )
     *
     *
     * @param constructor
     * @param routeProviderName
     * @return
     */
    @Override
    List<PsiElementContext> getRouteParams(PsiElementContext constructor, String routeProviderName) {

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
                .collect(Collectors.toList());

    }

    /**
     * returns the subelement hosting the route meta data
     *              {
     *                         name: "myState",
     *                         url:"/myState"
     *              }
     */
     PsiElementContext getRouteMeta(PsiElementContext routeParam) {
         return routeParam.queryContent(JS_ARGUMENTS_LIST, JS_OBJECT_LITERAL_EXPRESSION)
                .findFirst().get();
    }


}
