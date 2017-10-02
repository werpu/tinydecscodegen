package actions;

import actions.shared.IntellijRootData;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import utils.IntellijUtils;

import java.io.IOException;
import java.net.URLClassLoader;

public class DtoGeneration extends AnAction {

    private static final Logger log = Logger.getInstance(DtoGeneration.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = IntellijUtils.getProject(event);
        IntellijRootData intellijRootData = new IntellijRootData(event, project);
        if (intellijRootData.isError()) return;
        final Module module = intellijRootData.getModule();
        final String className = intellijRootData.getClassName();
        final PsiFile javaFile = intellijRootData.getJavaFile();


        //CompileStatusNotification compilerCallback = new CompileStatusNotification();
        CompilerManager.getInstance(project).compile(module, new CompileStatusNotification() {
            @Override
            public void finished(boolean b, int i, int i1, CompileContext compileContext) {
                ApplicationManager.getApplication().invokeLater(() -> compileDone(compileContext));
            }

            private boolean compileDone(CompileContext compileContext) {
                try {
                    URLClassLoader urlClassLoader = IntellijUtils.getClassLoader(compileContext, module);
                    IntellijUtils.generateDto(project, module, className, javaFile, urlClassLoader);
                } catch (RuntimeException | IOException | ClassNotFoundException e) {
                    log.error(e);
                    Messages.showErrorDialog(project, e.getMessage(), "An Error has occurred");
                }
                return false;
            }
        });

    }


}
