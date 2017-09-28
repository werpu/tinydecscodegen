import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import utils.IntellijUtils;

import java.io.IOException;
import java.net.URLClassLoader;

public class ServiceGenerateActionWithoutCompile extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = IntellijUtils.getProject(event);
        Editor editor = IntellijUtils.getEditor(event);
        if (editor == null) {
            Messages.showErrorDialog(project, "There is no editor selected", "No editor selected");
            return;
        }

        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);
        PsiClass clz = javaFile.getClasses()[0];

        Module module = IntellijUtils.getModuleFromEditor(project, editor);
        String className = IntellijUtils.getClassNameFromEditor(project, editor);


        try {
            URLClassLoader urlClassLoader = IntellijUtils.getClassLoader(module);
            IntellijUtils.generate(project, module, className, urlClassLoader);

        } catch (RuntimeException | IOException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(project, e.getMessage(), "An Error has occurred");
        }

    }


}

