package net.werpu.tools.actions_ng;

import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class CreateNgRouteFromEdit extends CreateNgRoute {
    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        if (!anActionEvent.getPresentation().isEnabledAndVisible()) {
            return;
        }

        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (VisibleAssertions.assertNotTs(ctx) ||
                !VisibleAssertions.assertController(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);

    }


    @NotNull
    public Optional<ComponentFileContext> getDefaultComponentData(IntellijFileContext fileContext) {
        ComponentFileContext compCtx = new ComponentFileContext(fileContext);
        return ofNullable(compCtx);
    }
}
