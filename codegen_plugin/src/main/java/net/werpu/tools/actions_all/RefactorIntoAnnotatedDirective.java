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
import net.werpu.tools.supportive.transformations.tinydecs.AngularJSDirectiveTransformationModel;
import net.werpu.tools.supportive.transformations.tinydecs.DirectiveTransformation;

import java.io.IOException;

import static net.werpu.tools.supportive.utils.IntellijUtils.*;

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
            Language typescript = getTypescriptLanguageDef();
            PsiFile transformed = createRamFileFromText(fileContext.getProject(),
                    transformationModel.getPsiFile().getName(),
                    /*reformat(fileContext.getProject(), typescript, transformation.getTnDecTransformation())*/ transformation.getTnDecTransformation(), typescript);
            showDiff(fileContext.getProject(), "Difference", fileContext.getPsiFile(), transformed, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

}
