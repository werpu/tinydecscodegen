package net.werpu.tools.actions_ng;

import net.werpu.tools.actions.ServiceGenerationAction;

public class NgServiceGenerationAction extends ServiceGenerationAction {

    @Override
    protected boolean isNg() {
        return true;
    }
}
