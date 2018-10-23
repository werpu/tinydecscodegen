package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class FilterPipeContext extends TypescriptResourceContext {
    public FilterPipeContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public FilterPipeContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public FilterPipeContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public FilterPipeContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }
}
