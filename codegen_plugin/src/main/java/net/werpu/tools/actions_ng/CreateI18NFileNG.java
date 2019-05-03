package net.werpu.tools.actions_ng;

import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions.CreateI18NFile;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

public class CreateI18NFileNG extends CreateI18NFile {
    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (IntellijUtils.getFolderOrFile(anActionEvent) == null ||
                !ctx.getVirtualFile().isDirectory()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        VisibleAssertions.ngVisible(anActionEvent);
    }
}
