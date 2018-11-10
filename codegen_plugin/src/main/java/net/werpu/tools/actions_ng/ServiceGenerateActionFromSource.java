package net.werpu.tools.actions_ng;

import net.werpu.tools.actions.ServiceGenerationAction;
import net.werpu.tools.actions_all.shared.JavaFileContext;
import net.werpu.tools.actions_all.shared.NgFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.*;

public class ServiceGenerateActionFromSource extends AnAction {

    private static final Logger log = Logger.getInstance(ServiceGenerationAction.class);


    @Override
    public void update(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        //TODO improve project angular detection
        //VisibleAssertions.ngVisible(anActionEvent);


        if (!VisibleAssertions.hasAngularVersion(anActionEvent, AngularVersion.NG) ||
                (!anActionEvent.getPresentation().isVisible()) || assertNotJava(ctx) || (assertNotJavaRest(ctx) && assertNotSpringRest(ctx))) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        if (event.getData(PlatformDataKeys.EDITOR) == null) {
            net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(event.getProject(), "Error", "No editor found, please focus on an open source file");
            return;
        }

        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.fileNameTransformer = new NgFileNameTransformer("service");
            IntellijUtils.generateService(javaData.getProject(), javaData.getModule(), (PsiJavaFile) javaData.getJavaFile(), true);
        } catch (RuntimeException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), net.werpu.tools.actions_all.shared.Messages.ERR_OCCURRED);
        }

    }
}

