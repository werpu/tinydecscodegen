/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.actions_all;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiFile;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.tinydecs.AngularJSModuleTransformationModel;
import net.werpu.tools.supportive.transformations.tinydecs.ModuleTransformation;
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
            if (!ctx.getText().contains(".module")) {
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
