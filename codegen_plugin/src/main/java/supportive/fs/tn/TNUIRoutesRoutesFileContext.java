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
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.*;
import static supportive.utils.StringUtils.literalEquals;

/**
 * helper context to deal with routes and subroutes
 * in a single file using the tiny decorations
 */
public class TNUIRoutesRoutesFileContext extends TypescriptFileContext implements IUIRoutesRoutesFileContext {

    @Getter
    List<PsiElementContext> constructors = Lists.emptyList();

    public TNUIRoutesRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
        init();
    }

    public TNUIRoutesRoutesFileContext(AnActionEvent event) {
        super(event);
        init();
    }

    public TNUIRoutesRoutesFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
        init();
    }

    public TNUIRoutesRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
        init();
    }


    protected void init() {

        constructors = this.queryContent(">" + TYPE_SCRIPT_FUNC, p_isConstructor(), p_isRouteProviderPresent()
        ).collect(Collectors.toList());
        
        /*constructors = new PsiElementContext(getPsiFile().getOriginalElement()).findPsiElements(PsiWalkFunctions::isTypeScriptFunc)
                .stream()
                .filter(el -> el.getName().equals("constructor"))
                .filter(el -> getRouteProviderDef(el).isPresent())
                .collect(Collectors.toList());*/
    }

    @NotNull
    private Predicate<PsiElementContext> p_isRouteProviderPresent() {
        return el -> getRouteProviderDef(el).isPresent();
    }

    @NotNull
    private Predicate<PsiElementContext> p_isConstructor() {
        return el -> el.getName().equals("constructor");
    }


    @Override
    public void addRoute(Route routeData) {
        //find route provider inject
        routeData.setComponent(super.appendImport(routeData.getComponent(), routeData.getComponentPath()));

        for (PsiElementContext constructor : constructors) {
            String routeProviderName = getRouteProviderName(constructor);


            Optional<PsiElementContext> body = constructor.findPsiElement(PsiWalkFunctions::isJSBlock);
            int insertPos = calculateRouteInsertPos(routeProviderName, constructor, body);


            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(insertPos), toRoute(routeProviderName, routeData)));
        }
    }


    public String getRouteProviderName(PsiElementContext constructor) {
        Optional<PsiElementContext> routeProviderDef = getRouteProviderDef(constructor);
        Optional<PsiElementContext> routeProvider = routeProviderDef.get().walkParent(PsiWalkFunctions::isTypeScriptParam);
        return routeProvider.get().getName();
    }


    private String toRoute(String provider, Route data) {
        String url = data.getUrl();
        if (isStateProvider(provider)) {
            String tpl = "\n$stateProvider.state('%s','%s', MetaData.routeData(%s));";

            return String.format(tpl, url, data.getComponent());
        } else {
            String tpl = (url.isEmpty()) ?
                    "\n$routeProvider.route('%s', MetaData.routeData(%s));" :
                    "\n$routeProvider.route('%s', MetaData.routeData(%s, {url: '" + url + "'}));";

            return String.format(tpl, data.getRouteKey(), data.getComponent());
        }
    }

    private boolean isStateProvider(String provider) {
        return provider.equals("$stateProvider");
    }

    public Optional<PsiElementContext> getRouteProviderDef(PsiElementContext constructor) {

        Optional<PsiElementContext> retVal = constructor.queryContent(p_isInject(), p_isProvider()).findFirst();
        
       /* Optional<PsiElementContext> retVal = constructor.findPsiElements(PsiWalkFunctions::isInject).stream()
                .flatMap(psiElementContext -> psiElementContext.findPsiElements(psiElement -> isProvider(psiElement)).stream()).findFirst();
*/
        //fallback we search for an identifier only with the name of $routeProvider
        if (!retVal.isPresent()) {
            retVal = constructor.queryContent(PSI_ELEMENT_JS_IDENTIFIER,
                    (Predicate<PsiElementContext>) ident -> isRouteProvider(ident.getText()) || isStateProvider(ident.getText())).findFirst();
            
            /*retVal = constructor.findPsiElements(PsiWalkFunctions::isIdentifier).stream()
                    .filter(ident -> isRouteProvider(ident.getText()) || isStateProvider(ident.getText()))
                    .findFirst();*/
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

    public int calculateRouteInsertPos(String routeProviderName, PsiElementContext constructor, Optional<PsiElementContext> body) {
        //now append the route to the contsructor after the last routprovider declaration or to the bottom
        List<PsiElementContext> whenCalls = getWhenCalls(constructor, routeProviderName);
        int insertPos = 0;
        if (whenCalls.size() > 0) {
            PsiElementContext elementContext = whenCalls.get(whenCalls.size() - 1);
            insertPos = elementContext.getElement().getStartOffsetInParent() + elementContext.getElement().getTextLength();
        } else {
            insertPos = body.get().getTextOffset() + 1;
        }
        return insertPos;
    }

    List<PsiElementContext> getWhenCalls(PsiElementContext constructor, String routeProviderName) {

        return constructor.queryContent(JS_BLOCK_ELEMENT, ":FIRST",
                JS_EXPRESSION_STATEMENT,
                p_isRouteCall(routeProviderName))
                .collect(Collectors.toList());
                

    }

    @NotNull
    private Predicate<PsiElementContext> p_isRouteCall(String routeProviderName) {
        return el -> isRouteCall(routeProviderName, el);
    }

    @NotNull
    private Predicate<PsiElementContext> getRouteCallPredicate(String routeProviderName) {
        return el -> isRouteCall(routeProviderName, el);
    }


    private boolean isRouteCall(String routeProviderName, PsiElementContext el) {
        return el.getText().startsWith(routeProviderName + ".when") || el.getText().startsWith(routeProviderName + ".state");
    }


    @Override
    public boolean isUrlInUse(Route routeData) {

        for (PsiElementContext constructor : constructors) {


            Optional<PsiElementContext> routeProviderDef = getRouteProviderDef(constructor);
            String routeProviderName = getRouteProviderName(constructor);

            if (!isRouteProvider(routeProviderName)) {//no url support in case of stateproviders
                return false;
            }

            if (urlMatch(routeData, constructor, routeProviderName)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    public Boolean urlMatch(Route routeData, PsiElementContext constructor, String routeProviderName) {
            //TODO also url match against the linked components, but for now this suffices
            return constructor.queryContent(JS_ARGUMENTS_LIST, ":PARENTS", JS_EXPRESSION_STATEMENT, "TEXT*:("+routeProviderName+")", PSI_ELEMENT_JS_STRING_LITERAL, "TEXT:('"+ routeData.getRouteKey()+"')").findFirst().isPresent();
    }


    public boolean isRouteProvider(String routeProviderName) {
        return routeProviderName.equals("$routeProvider");
    }

    @Override
    public boolean isRouteVarNameUsed(Route routeData) {
        return false;
    }

    @Override
    public boolean isRouteNameUsed(Route routeData) {
        for (PsiElementContext constructor : constructors) {


            Optional<PsiElementContext> routeProviderDef = getRouteProviderDef(constructor);
            String routeProviderName = getRouteProviderName(constructor);

            if (isRouteProvider(routeProviderName)) {
                return false;
            }

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

}
