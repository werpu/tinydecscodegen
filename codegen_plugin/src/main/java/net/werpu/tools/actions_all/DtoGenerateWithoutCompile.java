package net.werpu.tools.actions_all;

import net.werpu.tools.actions.ServiceGenerationAction;
import net.werpu.tools.actions_all.shared.JavaFileContext;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.io.IOException;

public class DtoGenerateWithoutCompile extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {

        JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.fileNameTransformer = new SimpleFileNameTransformer();
            IntellijUtils.generateDto(javaData.getProject(), javaData.getModule(), javaData.getClassName(), javaData.getJavaFile(), javaData.getClassLoader());

        } catch (RuntimeException | IOException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), net.werpu.tools.actions_all.shared.Messages.ERR_OCCURRED);
        }

    }
}