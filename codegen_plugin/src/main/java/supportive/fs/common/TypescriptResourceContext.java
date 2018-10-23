package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;

public class TypescriptResourceContext extends TypescriptFileContext {

    @Getter
    protected PsiElementContext resourceRoot;

    public TypescriptResourceContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public TypescriptResourceContext(AnActionEvent event) {
        super(event);
    }

    public TypescriptResourceContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public TypescriptResourceContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    public TypescriptResourceContext(Project project, PsiFile psiFile, PsiElement psiElement) {
        super(project, psiFile);
        this.resourceRoot = new PsiElementContext(psiElement);
    }

    public TypescriptResourceContext(AnActionEvent event, PsiElement psiElement) {
        super(event);
        this.resourceRoot = new PsiElementContext(psiElement);
    }

    public TypescriptResourceContext(Project project, VirtualFile virtualFile, PsiElement psiElement) {
        super(project, virtualFile);
        this.resourceRoot = new PsiElementContext(psiElement);
    }

    public TypescriptResourceContext(IntellijFileContext fileContext, PsiElement psiElement) {
        super(fileContext);
        this.resourceRoot = new PsiElementContext(psiElement);
    }
}
