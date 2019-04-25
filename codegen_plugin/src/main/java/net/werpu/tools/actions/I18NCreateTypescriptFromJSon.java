package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import org.jetbrains.annotations.NotNull;

/**
 * json->typescript i18n conversion (we will add a refactoring for the templates later
 * for now it is just a simple conversion)
 *
 */
public class I18NCreateTypescriptFromJSon extends AnAction {

    @Override
    public void update(AnActionEvent anActionEvent) {
        //only visible on json i18n files
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }


}
