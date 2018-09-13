package supportive.fs.tn;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import org.fest.util.Lists;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.*;
import supportive.reflectRefact.PsiWalkFunctions;
import supportive.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.valueOf;
import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.literalEquals;

public abstract class  TNRoutesFileContext extends TypescriptFileContext implements IUIRoutesRoutesFileContext {
    @Getter
    List<PsiElementContext> constructors = Lists.emptyList();

    public TNRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public TNRoutesFileContext(AnActionEvent event) {
        super(event);
    }

    public TNRoutesFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public TNRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    @NotNull
    protected Predicate<PsiElementContext> p_isRouteProviderPresent() {
        return el -> getRouteProviderDef(el).isPresent();
    }

    @NotNull
    protected Predicate<PsiElementContext> p_isStateProviderPresent() {
        return el -> getStateProviderDef(el).isPresent();
    }

    public Optional<PsiElementContext> getSateOrRouteProviderDef(PsiElementContext constructor) {

        Optional<PsiElementContext> retVal = constructor.queryContent(p_isInject(), p_isProvider()).findFirst();


        if (!retVal.isPresent()) {
            retVal = constructor.queryContent(PSI_ELEMENT_JS_IDENTIFIER,
                    (Predicate<PsiElementContext>) ident -> isRouteProvider(ident.getText()) || isStateProvider(ident.getText())).findFirst();
        }

        return retVal;
    }

    public Optional<PsiElementContext> getStateProviderDef(PsiElementContext constructor) {

        Optional<PsiElementContext> retVal = constructor.queryContent(p_isInject(), p_isProvider()).findFirst();


        if (!retVal.isPresent()) {
            retVal = constructor.queryContent(PSI_ELEMENT_JS_IDENTIFIER,
                    (Predicate<PsiElementContext>)  ident -> isStateProvider(ident.getText())).findFirst();
        }

        return retVal;
    }


    public Optional<PsiElementContext> getRouteProviderDef(PsiElementContext constructor) {

        Optional<PsiElementContext> retVal = constructor.queryContent(p_isInject(), p_isProvider()).findFirst();


        if (!retVal.isPresent()) {
            retVal = constructor.queryContent(PSI_ELEMENT_JS_IDENTIFIER,
                    (Predicate<PsiElementContext>)  ident -> isRouteProvider(ident.getText())).findFirst();
        }

        return retVal;
    }


    @NotNull
    public Predicate<PsiElementContext> p_isProvider() {
        return el -> isProvider(el.getElement());
    }

    @NotNull
    public Predicate<PsiElementContext> p_isInject() {
        return el -> PsiWalkFunctions.isInject(el.getElement());
    }

    public boolean isProvider(PsiElement psiElement) {
        return isStringLiteral(psiElement) &&
                (psiElement.getText().equals("\"$routeProvider\"") ||
                        psiElement.getText().equals("'$routeProvider'") ||
                        psiElement.getText().equals("\"$stateProvider\"") ||
                        psiElement.getText().equals("'$stateProvider'")
                );
    }

    @NotNull
    protected Predicate<PsiElementContext> p_isConstructor() {
        return el -> el.getName().equals("constructor");
    }

    public boolean isRouteProvider(String routeProviderName) {
        return routeProviderName.equals("$routeProvider");
    }

    public boolean isStateProvider(String provider) {
        return provider.equals("$stateProvider");
    }

    protected String toRoute(String provider, Route data) {
        String url = data.getUrl();
        if (isStateProvider(provider)) {
            String tpl = (url.isEmpty()) ? "\n$stateProvider.state('%s','%s', MetaData.routeData(%s));" :
                    "\n$stateProvider.state('%s','%s', MetaData.routeData(%s), {url: '" + url + "'});";


            return String.format(tpl, url, data.getRouteKey(), data.getComponent());
        } else {
            String tpl = "\n$routeProvider.when('%s', MetaData.routeData(%s));";

            return String.format(tpl, data.getUrl(), data.getComponent());
        }
    }

    public Optional<String> resolveJsVar(PsiElementContext call) {
        //$routeProvider.when("/view1", MetaData.routeData(View1));
        return call.findPsiElements(psiElement -> psiElement.getText().startsWith("MetaData.routeData")).stream()
                .map(item -> item.queryContent(PSI_ELEMENT_JS_IDENTIFIER, P_LAST).findFirst().get())
                .map(item -> item.getText()).findFirst();

    }

    public Optional<PsiElementContext> refOrLiteral(Optional<PsiElementContext> args) {
        Optional<PsiElementContext> refExpr = args.get().queryContent(JS_REFERENCE_EXPRESSION).findFirst();
        Optional<PsiElementContext> litExpr = args.get().queryContent(JS_OBJECT_LITERAL_EXPRESSION).findFirst();
        Optional<PsiElementContext> sLitExpr = args.get().queryContent(PSI_ELEMENT_JS_STRING_LITERAL).findFirst();

        List<Optional<PsiElementContext>> calcModel = new ArrayList<>(3);
        calcModel.add(refExpr);
        calcModel.add(litExpr);
        calcModel.add(sLitExpr);

        return calcModel.stream()
                .filter(o1 -> o1.isPresent())
                .sorted((o1, o2) -> valueOf(o1.get().getTextOffset()).compareTo(o2.get().getTextOffset()))
                .findFirst().orElse(Optional.empty());
    }

    public Optional<PsiRouteContext> mapArg(PsiElementContext call, PsiElementContext arg) {
        Optional<PsiElementContext> oUrl = refOrLiteral(Optional.of(arg));
        Optional<String> classIdentifier = resolveJsVar(call);
        Optional<PsiElement> importPath = findImportString(classIdentifier.get()); //todo else handling
        if (!importPath.isPresent()) {
            return Optional.empty();
        }
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
        Route route = new Route(name, url, component, "", pageController.getVirtualFile().getPath());

        return Optional.of(new PsiRouteContext(call.getElement(), route));
    }

    @NotNull
    public Function<PsiElementContext, Stream<Optional<PsiRouteContext>>> resolveArgs() {
        return (PsiElementContext call) -> {//$provider.when
            List<PsiElementContext> args = call.queryContent(JS_ARGUMENTS_LIST).collect(Collectors.toList());
            return args.stream().map(arg -> mapArg(call, arg));
        };
    }



    @NotNull
    public Boolean urlMatch(Route routeData, PsiElementContext constructor, String routeProviderName) {
        //TODO also url match against the linked components, but for now this suffices
        return constructor.queryContent(JS_ARGUMENTS_LIST, ":PARENTS", JS_EXPRESSION_STATEMENT, "TEXT*:(" + routeProviderName + ")", PSI_ELEMENT_JS_STRING_LITERAL, "TEXT:('" + routeData.getUrl() + "')").findFirst().isPresent();
    }

    public String getStateOrRouteProviderName(PsiElementContext constructor) {
        Optional<PsiElementContext> routeProviderDef = getSateOrRouteProviderDef(constructor);
        Optional<PsiElementContext> routeProvider = routeProviderDef.get().walkParent(PsiWalkFunctions::isTypeScriptParam);
        return routeProvider.get().getName();
    }

    @Override
    public boolean isUrlInUse(Route routeData) {

        for (PsiElementContext constructor : constructors) {


            Optional<PsiElementContext> routeProviderDef = getSateOrRouteProviderDef(constructor);
            String routeProviderName = getStateOrRouteProviderName(constructor);

           // if (!isRouteProvider(routeProviderName)) {//no url support in case of stateproviders
           //     return false;
           // }

            if (urlMatch(routeData, constructor, routeProviderName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isRouteVarNameUsed(Route routeData) {
        return false;
    }

    @Override
    public boolean isRouteNameUsed(Route routeData) {
        for (PsiElementContext constructor : constructors) {

            boolean found = constructor.queryContent(
                    PSI_ELEMENT_JS_STRING_LITERAL,
                    (Predicate<PsiElementContext>) el -> literalEquals(el.getName(),
                            routeData.getRouteKey())).findFirst().isPresent();
            if (found) {
                return true;
            }
        }
        return false;
    }


    public abstract List<PsiRouteContext> getRoutes();

    abstract List<PsiElementContext> getRouteParams(PsiElementContext constructor, String routeProviderName);

}