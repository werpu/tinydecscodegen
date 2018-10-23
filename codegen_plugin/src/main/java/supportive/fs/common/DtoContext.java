package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class DtoContext extends TypescriptResourceContext {
    public DtoContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public DtoContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public DtoContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public DtoContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }
}
