package net.werpu.tools.actions_all;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiFile;
import net.werpu.tools.gui.Refactoring;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSComponentTransformationModel;
import net.werpu.tools.supportive.transformations.ComponentTransformation;
import net.werpu.tools.supportive.transformations.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.TransformationDialogBuilder;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.awt.*;
import java.io.IOException;

import static com.intellij.ide.scratch.ScratchFileCreationHelper.reformat;

/**
 * action endpoint for the component refactoring dialog
 */
public class RefactorIntoAnnotatedComponent extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        try {
            IntellijFileContext ctx = new IntellijFileContext(e);
            e.getPresentation().setEnabledAndVisible(false);
            if (ctx.getText().contains("bindings") || ctx.getText().contains("IComponentOptions")) {
                AngularJSComponentTransformationModel model = new AngularJSComponentTransformationModel(new IntellijFileContext(e));
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


        AngularJSComponentTransformationModel transformationModel = new AngularJSComponentTransformationModel(fileContext);
        ComponentTransformation transformation = new ComponentTransformation(transformationModel);

        try {
            Language typescript = IntellijUtils.getTypescriptLanguageDef();
            PsiFile transformed = IntellijUtils.createRamFileFromText(fileContext.getProject(),
                    transformationModel.getPsiFile().getName(),
                    reformat(fileContext.getProject(), typescript, transformation.getTnDecTransformation()), typescript);
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
