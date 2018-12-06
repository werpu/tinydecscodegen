package net.werpu.tools.actions_all;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSComponentTransformationModel;
import net.werpu.tools.supportive.transformations.AngularJSDirectiveTransformationModel;
import net.werpu.tools.supportive.transformations.ComponentTransformation;
import net.werpu.tools.supportive.transformations.DirectiveTransformation;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.io.IOException;

import static com.intellij.ide.scratch.ScratchFileCreationHelper.reformat;

/**
 * action endpoint for the component refactoring dialog
 */
public class RefactorIntoAnnotatedDirective extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        try {
            IntellijFileContext ctx = new IntellijFileContext(e);
            e.getPresentation().setEnabledAndVisible(false);
            if (ctx.getText().contains("link") || ctx.getText().contains("restrict") || ctx.getText().contains("bindToController") || ctx.getText().contains("replace")) {
                AngularJSDirectiveTransformationModel model = new AngularJSDirectiveTransformationModel(new IntellijFileContext(e));
                e.getPresentation().setEnabledAndVisible(model.getConstructorBlock().isPresent());
            }
        } catch (Throwable t) {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        //
        final IntellijFileContext fileContext = new IntellijFileContext(event);


        AngularJSDirectiveTransformationModel transformationModel = new AngularJSDirectiveTransformationModel(fileContext);
        DirectiveTransformation transformation = new DirectiveTransformation(transformationModel);

        try {
            Language typescript = IntellijUtils.getTypescriptLanguageDef();
            PsiFile transformed = IntellijUtils.createRamFileFromText(fileContext.getProject(),
                    transformationModel.getPsiFile().getName(),
                    /*reformat(fileContext.getProject(), typescript, transformation.getTnDecTransformation())*/ transformation.getTnDecTransformation(), typescript);
            IntellijUtils.showDiff(fileContext.getProject(), "Difference", fileContext.getPsiFile(), transformed, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean isDumbAware() {
        return true;
    }


}
