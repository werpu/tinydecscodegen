package net.werpu.tools.actions_all;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSComponentTransformationModel;
import net.werpu.tools.supportive.transformations.AngularJSModuleTransformationModel;

/**
 * action endpoint for the component refactoring dialog
 */
public class RefactorIntoAnnotatedComponent  extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        try {
            IntellijFileContext ctx = new IntellijFileContext(e);
            if(!ctx.getText().contains("controller")) {
                AngularJSComponentTransformationModel model = new AngularJSComponentTransformationModel(new IntellijFileContext(e));
                e.getPresentation().setEnabledAndVisible(model.getConstructorBlock().isPresent());
            }
        } catch (Throwable t) {
            e.getPresentation().setEnabledAndVisible(false);
        }


    }

    @Override
    public void actionPerformed(AnActionEvent e) {

    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
