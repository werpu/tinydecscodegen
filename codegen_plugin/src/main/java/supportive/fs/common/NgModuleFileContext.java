package supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

/**
 * placeholder for a module file context
 *
 * this is a context which basically implements
 * all the required add operations for our artifacts, including
 * also the includes statement and omitting double insers
 *
 */
public class NgModuleFileContext extends TypescriptFileContext {
    public NgModuleFileContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public NgModuleFileContext(AnActionEvent event) {
        super(event);
    }

    public NgModuleFileContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public NgModuleFileContext(IntellijFileContext fileContext) {
        super(fileContext);
    }
}
