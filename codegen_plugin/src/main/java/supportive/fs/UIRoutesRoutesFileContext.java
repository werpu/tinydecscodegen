package supportive.fs;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.refactor.DummyInsertPsiElement;
import supportive.refactor.RefactorUnit;

import java.util.List;
import java.util.Optional;

import static supportive.utils.StringUtils.elVis;

/**
 * helper context to deal with routes and subroutes
 * in a single file
 */
public class UIRoutesRoutesFileContext extends TypescriptFileContext {

    PsiElementContext routesDeclaration;
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
    public void addRoute(Route routeData) {
        addRefactoring(new RefactorUnit(super.getPsiFile(), new DummyInsertPsiElement(routesDeclaration.getElement().getTextOffset()), routeData.toStringNg2()));
        addNavVar(routeData.getRouteVarName());
    }


    @NotNull
    public Optional<PsiElementContext> getNavigationalArray() {

        List<PsiElement> els = findPsiElements(el -> {
            return (el.toString().equals("JSCallExpression")) && el.getText().startsWith("UIRouterModule.forRoot");
        });
        return els.stream().map(el -> new PsiElementContext(el))
                .flatMap(elc -> elc.findPsiElements(el -> el.toString().equals("JSProperty") && elVis(el, "nameIdentifier", "text").isPresent() && elVis(el, "nameIdentifier", "text").get().equals("states")).stream())
                .map(elc -> elc.findPsiElement(el -> el.toString().equals("JSArrayLiteralExpression"))).findFirst().get();
    }

    public void addNavVar(String varName) {
        Optional<PsiElementContext> closingBracket = getEndOfNavArr();
        if(closingBracket.isPresent()) {
            addRefactoring(new RefactorUnit(getPsiFile(), new DummyInsertPsiElement(closingBracket.get().getElement().getTextOffset()), ", " + varName));
        }
    }

    @Nullable
    public Optional<PsiElementContext> getEndOfNavArr() {
        PsiElementContext navArr = getNavigationalArray().get();

        //find the last closing bracket
        return Optional.ofNullable(navArr
                .findPsiElements(el -> el.toString().equals("PsiElement(JS:RBRACKET)")).stream() //TODO type check once debugged out
                .reduce((first, second) -> second).orElse(null));
    }
}
