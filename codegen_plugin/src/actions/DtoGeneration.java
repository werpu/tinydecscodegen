package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import utils.IntellijUtils;

import java.io.IOException;
import java.net.URLClassLoader;

public class DtoGeneration extends AnAction {

        private static final Logger log = Logger.getInstance(DtoGeneration.class);

    @Override
    public void actionPerformed(AnActionEvent event)
    {
        Project project = IntellijUtils.getProject(event);
        Editor editor = IntellijUtils.getEditor(event);
        if(editor == null) {
            log.error("No editor found, please focus on a source file with a java type");
            return;
        }

        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(vFile);

        //clz.getContainingFile()
        //clz.getAllMethods()[3].getParameterList().getParameters()[0].getModifierList().getAnnotations()[0].getParameterList().getAttributes()[0].getValue().

        Module module = IntellijUtils.getModuleFromEditor(project, editor);
        String className = IntellijUtils.getClassNameFromEditor(project, editor);


        //CompileStatusNotification compilerCallback = new CompileStatusNotification();
        CompilerManager.getInstance(project).compile(module, new CompileStatusNotification() {
            @Override
            public void finished(boolean b, int i, int i1, CompileContext compileContext) {
                ApplicationManager.getApplication().invokeLater(() -> compileDone(compileContext));
            }

            private boolean compileDone(CompileContext compileContext) {
                try {
                    URLClassLoader urlClassLoader = IntellijUtils.getClassLoader(compileContext, module);
                    IntellijUtils.generateDto(project, module, className, urlClassLoader);
                } catch (RuntimeException | IOException | ClassNotFoundException e) {
                    log.error(e);
                    Messages.showErrorDialog(project, e.getMessage(), "An Error has occurred");
                }
                return false;
            }
        });

    }
}
