package actions;

import actions.shared.JavaFileContext;
import actions.shared.SimpleFileNameTransformer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import utils.IntellijUtils;

import java.io.IOException;

public class DtoGeneration extends AnAction {

    private static final Logger log = Logger.getInstance(DtoGeneration.class);

    @Override
    public void actionPerformed(AnActionEvent event) {
        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;


        CompilerManager.getInstance(javaData.getProject()).compile(javaData.getModule(), new CompileStatusNotification() {
            @Override
            public void finished(boolean b, int i, int i1, CompileContext compileContext) {
                ApplicationManager.getApplication().invokeLater(() -> compileDone(compileContext));
            }

            private boolean compileDone(CompileContext compileContext) {
                try {
                    IntellijUtils.fileNameTransformer = new SimpleFileNameTransformer();
                    IntellijUtils.generateDto(javaData.getProject(), javaData.getModule(), javaData.getClassName(), javaData.getJavaFile(), javaData.getClassLoader(compileContext));
                } catch (RuntimeException | IOException | ClassNotFoundException e) {
                    log.error(e);
                    Messages.showErrorDialog(javaData.getProject(), e.getMessage(), actions.Messages.ERR_OCCURRED);
                }
                return false;
            }
        });

    }


}
