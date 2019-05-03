package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

public class CreateI18NFile extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if(IntellijUtils.getFolderOrFile(anActionEvent) == null ||
                !ctx.getVirtualFile().isDirectory()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
