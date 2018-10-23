package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class ServiceContext extends TypescriptResourceContext {
    public ServiceContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public ServiceContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public ServiceContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public ServiceContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }
}
