package utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class ComponentFileContext extends TypescriptFileContext {

    PsiElement templateText;

    public ComponentFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public ComponentFileContext(AnActionEvent event) {
        super(event);
    }

    public ComponentFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public ComponentFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    public Optional<String> getTemplateText() {

        Optional<PsiElement> template = super.findPsiElements(PsiWalkFunctions::isTemplate).stream().findFirst();
        if(template.isPresent()) {
            Optional<PsiElement> retVal = Arrays.stream(template.get().getChildren()).filter(el -> {
                return PsiWalkFunctions.isTemplateString(el);
            }).findFirst();
            if(retVal.isPresent()) {
                this.templateText = retVal.get();
                return Optional.ofNullable(this.templateText.getText().substring(1, this.templateText.getText().length() - 1));
            }
        }
        return Optional.empty();
    }

    private PsiElement fetchStringContentElement(Optional<PsiElement> retVal) {
        return (PsiElement) retVal.get().getNode().getFirstChildNode().getTreeNext();
    }

    public void setTemplateText(String newText) {
        //TODO replace an existing refactoring if the text is set anew
        super.addRefactoring(new RefactorUnit(psiFile, this.templateText,"`" +newText+"`"));
    }

    @Override
    public void commit() throws IOException {
        super.commit();
    }

}
