package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.NG_PRJ_MARKER;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TN_DEC_PRJ_MARKER;
import static net.werpu.tools.supportive.utils.IntellijUtils.*;

public class MarkProjectAsTnDec extends AnAction {

    @Override
    public void update(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (ctx.isAngularChild(AngularVersion.NG)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        if (ctx.isAngularChild(AngularVersion.TN_DEC)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        anActionEvent.getPresentation().setEnabledAndVisible(ctx.getVirtualFile().isDirectory());

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        IntellijFileContext ctx = new IntellijFileContext(e);
        VirtualFile parFolder = ctx.getVirtualFile().isDirectory() ? ctx.getVirtualFile() : ctx.getVirtualFile().getParent();

        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                create(ctx.getProject(), parFolder, "", TN_DEC_PRJ_MARKER);
                refresh();
                ApplicationManager.getApplication().invokeLater(() -> {
                    refresh();
                    showInfoMessage("The folder now is an angular project", "Info");
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                showErrorDialog(ctx.getProject(), "An error has occurred during project marking check your error log for more details", "Error in project marking");
            }
        });

    }


}
