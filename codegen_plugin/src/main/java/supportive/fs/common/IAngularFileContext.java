package supportive.fs.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public interface IAngularFileContext {
    String getDisplayName();

    String getResourceName();

    NgModuleFileContext getParentModule();

    VirtualFile getVirtualFile();

    PsiFile getPsiFile();
}
