package actions;

import actions.shared.IntellijRootData;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import utils.IntellijUtils;

import java.io.IOException;
import java.net.URLClassLoader;

public class ServiceGenerateActionWithoutCompile extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void actionPerformed(AnActionEvent event) {

        Project project = IntellijUtils.getProject(event);
        IntellijRootData intellijRootData = new IntellijRootData(event, project);
        if (intellijRootData.isError()) return;
        final Module module = intellijRootData.getModule();
        final String className = intellijRootData.getClassName();
        final PsiFile javaFile = intellijRootData.getJavaFile();

        try {
            URLClassLoader urlClassLoader = IntellijUtils.getClassLoader(module);
            IntellijUtils.generate(project, module, className, javaFile, urlClassLoader);

        } catch (RuntimeException | IOException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(project, e.getMessage(), "An Error has occurred");
        }

    }
}

