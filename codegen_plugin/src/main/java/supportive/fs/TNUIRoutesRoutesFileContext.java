package supportive.fs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import indexes.ComponentIndex;
import org.fest.util.Lists;
import org.jetbrains.annotations.NotNull;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static supportive.reflectRefact.PsiWalkFunctions.isStringLiteral;
import static supportive.utils.StringUtils.findWithSpaces;
import static supportive.utils.StringUtils.literalContains;

/**
 * helper context to deal with routes and subroutes
 * in a single file using the tiny decorations
 */
public class TNUIRoutesRoutesFileContext extends TypescriptFileContext implements IUIRoutesRoutesFileContext {

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
        constructors = new PsiElementContext(getPsiFile().getOriginalElement()).findPsiElements(PsiWalkFunctions::isTypeScriptFunc)
                .stream()
                .filter(el -> el.getName().equals("constructor"))
                .filter(el -> getRouteProviderDef(el).isPresent())
                .collect(Collectors.toList());
    }


    @Override
    public void addRoute(Route routeData) {
        //find route provider inject

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
                    "\n$routeProvider.route('%s', MetaData.routeData(%s, {'" + url + "'}));";

            return String.format(tpl, data.getRouteKey(), data.getComponent());
        }
    }

    private boolean isStateProvider(String provider) {
        return provider.equals("$stateProvider");
    }

    public Optional<PsiElementContext> getRouteProviderDef(PsiElementContext constructor) {

        Optional<PsiElementContext> retVal = constructor.findPsiElements(PsiWalkFunctions::isInject).stream()
                .flatMap(psiElementContext -> psiElementContext.findPsiElements(psiElement -> {
                    return isStringLiteral(psiElement) &&
                            (psiElement.getText().equals("\"$routeProvider\"") ||
                                    psiElement.getText().equals("'$routeProvider'") ||
                                    psiElement.getText().equals("\"$stateProvider\"") ||
                                    psiElement.getText().equals("'$stateProvider'")
                            );
                }).stream()).findFirst();

        //fallback we search for an identifier only with the name of $routeProvider
        if (!retVal.isPresent()) {
            retVal = constructor.findPsiElements(PsiWalkFunctions::isIdentifier).stream()
                    .filter(ident -> isRouteProvider(ident.getText()) || isStateProvider(ident.getText()))
                    .findFirst();
        }

        return retVal;
    }

    public int calculateRouteInsertPos(String routeProviderName, PsiElementContext constructor, Optional<PsiElementContext> body) {
        //now append the route to the contsructor after the last routprovider declaration or to the bottom
        List<PsiElementContext> whenCalls = getWhenCalls(constructor, routeProviderName);
        int insertPos = 0;
        if (whenCalls.size() > 0) {
            PsiElementContext elementContext = whenCalls.get(whenCalls.size() - 1);
            insertPos = elementContext.getElement().getStartOffsetInParent() + elementContext.getElement().getTextLength();
        } else {
            insertPos = body.get().getElement().getStartOffsetInParent() + 1;
        }
        return insertPos;
    }

    List<PsiElementContext> getWhenCalls(PsiElementContext constructor, String routeProviderName) {

        return constructor
                .findPsiElement(PsiWalkFunctions::isJSBlock) //body
                .get()
                .findPsiElements(PsiWalkFunctions::isJSExpressionStatement).stream()
                .filter(el -> isRouteCall(routeProviderName, el))
                .collect(Collectors.toList());
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
        if (isRouteProvider(routeProviderName)) {
            return getWhenCalls(constructor, routeProviderName).stream().map(whenCall -> whenCall.findPsiElements(PsiWalkFunctions::isJSArgumentsList).stream()
                    .map(argumentsList -> argumentsList.findPsiElements(PsiWalkFunctions::isStringLiteral).stream()
                            .filter(literal -> literal.getText().equals("\"" + routeData.getUrl() + "\"") ||
                                    literal.getText().equals("'" + routeData.getUrl() + "'")
                            ).findFirst())
                    .filter(predicate -> predicate.isPresent()).findFirst().isPresent()).findFirst().get();
        } else {
            //we need to list all components
            Optional<IntellijFileContext> angularRoot = getAngularRoot();
            return ComponentIndex.getAllComponentFiles(getProject()).stream()
                    .map(componentPsiFile -> new ComponentFileContext(getProject(), componentPsiFile))
                    .filter(componentFileContext -> isChildComponent(angularRoot, componentFileContext))
                    .filter(componentFileContext -> urlMatch(routeData, componentFileContext))
                    .findFirst().isPresent();
        }
    }

    private boolean isChildComponent(Optional<IntellijFileContext> angularRoot, ComponentFileContext componentFileContext) {
        return (!angularRoot.isPresent()) || componentFileContext.isChildOf(angularRoot.get());
    }

    private boolean urlMatch(Route routeData, ComponentFileContext componentFileContext) {
        return findWithSpaces(componentFileContext.getPsiFile().getText(), "url", ":", "'" + routeData.getUrl() + "'") ||
                findWithSpaces(componentFileContext.getPsiFile().getText(), "url", ":", "\"" + routeData.getUrl() + "\"");
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


            if (literalContains(constructor.getText(), routeData.getRouteKey()) &&
                    (findWithSpaces(constructor.getText(), "(", "\"" + routeData.getRouteKey() + "\"")) ||
                    findWithSpaces(constructor.getText(), "(", "'" + routeData.getRouteKey() + "'")) {
                return true;
            }

        }
        return false;
    }

}
