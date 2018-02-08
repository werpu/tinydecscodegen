package supportive.fs;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;
import supportive.reflectRefact.PsiWalkFunctions;

import java.util.List;
import java.util.Optional;

import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.literalContains;

/**
 * helper context to deal with routes and subroutes
 * in a single file
 */
public class UIRoutesRoutesFileContext extends TypescriptFileContext implements IUIRoutesRoutesFileContext {


    PsiElementContext routesArr;


    public UIRoutesRoutesFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public UIRoutesRoutesFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }


    /**
     * adds a new route to the existing file
     * TODO also add the include if possible
     *
     * @param routeData
     */
    @Override
    public void addRoute(Route routeData) {

        //todo check if navvar already exists:
        String origRouteVarName = routeData.getRouteVarName();
        int cnt = 1;
        while (isRouteVarNameUsed(routeData)) {
            routeData.setRouteVarName(origRouteVarName + "_" + cnt);
            cnt++;
        }

        cnt = 1;
        String origUrl = routeData.getUrl();

               
        while (isUrlInUse(routeData)) {
            routeData.setUrl(origUrl + "_" + cnt);
            cnt++;
        }

        if (!getPsiFile().getText().contains(routeData.getInclude())) {
            appendImport("\n" + routeData.getInclude());
        }
        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(getRoutesDeclration().get().getRootElement().getElement().getTextOffset()), routeData.toStringNg2()));
        addNavVar(routeData.getRouteVarName());
    }

    @Override
    public boolean isUrlInUse(Route routeData) {
        return urlCheck(routeData, getPsiFile().getText());
    }

    @Override
    public boolean isRouteVarNameUsed(Route routeData) {
        return getNavigationalArray().get().findPsiElements(PsiWalkFunctions::isIdentifier).stream()
                .filter(psiElementContext -> psiElementContext.getElement().getText().equals(routeData.getRouteVarName())).findAny().isPresent();
    }

    @Override
    public boolean isRouteNameUsed(Route routeData) {
        return getPsiFile().getText().contains("name: '"+routeData.getRouteKey()+"'");
    }

    public boolean urlCheck(Route routeData, String fullText) {
        return literalContains(fullText, routeData.getUrl());
    }

    @NotNull
    public Optional<PsiElementContext> getRoutesDeclration() {

        List<PsiElement> els = findPsiElements(PsiWalkFunctions::isRootNav);
        return els.stream().map(el -> new PsiElementContext(el)).findFirst();
    }


    @NotNull
    public Optional<PsiElementContext> getNavigationalArray() {

        List<PsiElement> els = findPsiElements(PsiWalkFunctions::isRootNav);
        return els.stream().map(el -> new PsiElementContext(el))
                .flatMap(elc -> elc.findPsiElements(el -> el.toString().startsWith(PsiWalkFunctions.JS_PROPERTY) && elVis(el, "nameIdentifier", "text").isPresent() && elVis(el, "nameIdentifier", "text").get().equals("states")).stream())
                .map(elc -> elc.findPsiElement(el -> el.toString().startsWith(PsiWalkFunctions.JS_ARRAY_LITERAL_EXPRESSION))).findFirst().get();
    }

    public void addNavVar(String varName) {
        Optional<PsiElementContext> closingBracket = getEndOfNavArr();
        if (closingBracket.isPresent()) {
            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(closingBracket.get().getElement().getTextOffset()), ", " + varName));
        }
    }

    @Nullable
    public Optional<PsiElementContext> getEndOfNavArr() {
        PsiElementContext navArr = getNavigationalArray().get();

        //find the last closing bracket
        return Optional.ofNullable(navArr
                .findPsiElements(el -> el.toString().equals(PsiWalkFunctions.PSI_ELEMENT_JS_RBRACKET)).stream() //TODO type check once debugged out
                .reduce((first, second) -> second).orElse(null));
    }


    //only one route el per file allowed atm
}
