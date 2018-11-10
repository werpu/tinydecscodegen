package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Action handler for morping the current file (if it is an angular 1 module)
 * to a tiny decorations module
 */
public class MorphToTnDecModule extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
