package actions;

import actions.shared.JavaFileContext;
import actions.shared.SimpleFileNameTransformer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import utils.IntellijUtils;

import java.io.IOException;

public class ServiceGenerateActionWithoutCompile extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {

        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.fileNameTransformer = new SimpleFileNameTransformer();
            IntellijUtils.generateService(javaData.getProject(), javaData.getModule(), javaData.getClassName(), javaData.getJavaFile(), javaData.getClassLoader(), false);
        } catch (RuntimeException | IOException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), actions.Messages.ERR_OCCURRED);
        }

    }
}

