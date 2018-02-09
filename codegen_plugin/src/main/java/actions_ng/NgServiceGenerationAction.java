package actions_ng;

import actions.ServiceGenerationAction;

public class NgServiceGenerationAction extends ServiceGenerationAction {

    @Override
    protected boolean isNg() {
        return true;
    }
}
