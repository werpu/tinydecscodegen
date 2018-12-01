package net.werpu.tools.actions_all;


import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSModuleTransformationModel;
import net.werpu.tools.supportive.transformations.ModuleTransformation;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.awt.*;
import java.io.IOException;

import static com.intellij.ide.scratch.ScratchFileCreationHelper.reformat;

/**
 * Refactor module into a new TNDecModule
 */
public class RefactorIntoAngularModule extends AnAction {

    private static final String DIMENSION_KEY = "AnnRef";
    private static final String DLG_TITLE = "Module Code Proposal";
    private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        try {
            IntellijFileContext ctx = new IntellijFileContext(e);
            if(!ctx.getText().contains(".module")) {
                AngularJSModuleTransformationModel module = new AngularJSModuleTransformationModel(e);
                e.getPresentation().setEnabledAndVisible(module.getModuleDeclStart().isPresent());
            }
        } catch (Throwable t) {
            e.getPresentation().setEnabledAndVisible(false);
        }


    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        final IntellijFileContext fileContext = new IntellijFileContext(event);


        AngularJSModuleTransformationModel transformationModel = new AngularJSModuleTransformationModel(fileContext);
        ModuleTransformation transformation = new ModuleTransformation(transformationModel);

        try {
            Language typescript = IntellijUtils.getTypescriptLanguageDef();
            PsiFile transformed = IntellijUtils.createRamFileFromText(fileContext.getProject(),
                    transformationModel.getPsiFile().getName(),
                    reformat(fileContext.getProject(), typescript, transformation.getNgTransformation()), typescript);
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
