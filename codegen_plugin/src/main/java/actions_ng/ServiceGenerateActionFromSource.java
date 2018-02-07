package actions_ng;

import actions.ServiceGenerationAction;
import actions.shared.JavaFileContext;
import actions.shared.NgFileNameTransformer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import supportive.fs.IntellijFileContext;
import supportive.utils.IntellijUtils;

import static actions.shared.VisibleAssertions.assertNotJava;
import static actions.shared.VisibleAssertions.assertNotJavaRest;

public class ServiceGenerateActionFromSource extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);

    @Override
    public void update(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (assertNotJava(ctx) || assertNotJavaRest(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        if (event.getData(PlatformDataKeys.EDITOR) == null) {
            com.intellij.openapi.ui.Messages.showErrorDialog(event.getProject(), "Error", "No editor found, please focus on an open source file");
            return;
        }

        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.fileNameTransformer = new NgFileNameTransformer("service");
            IntellijUtils.generateService(javaData.getProject(), javaData.getModule(), (PsiJavaFile) javaData.getJavaFile(), true);
        } catch (RuntimeException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), actions.Messages.ERR_OCCURRED);
        }

    }
}

