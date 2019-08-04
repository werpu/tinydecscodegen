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


import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiI18nEntryContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.i18n.I18NJsonDeclFileTransformation;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.werpu.tools.supportive.utils.IntellijRunUtils.invokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;

/**
 * similar marker on the json side compared to the
 * Typescript side
 *
 */
public class MarkAsI18NJsonFile extends MarkAsI18NTSFile {
    //we inherit a lot of functionality only the visibility scope and the refactoring is different

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (!IntellijUtils.isJSON(ctx.getVirtualFile().getFileType())) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        //funnily we can recycle the i18n file pattern detection
        assertI18nPattern(anActionEvent, ctx);
    }


    protected void addMarker(TypescriptFileContext ctx,final PsiI18nEntryContext elCtx) {

        invokeLater(() -> writeTransaction(ctx.getProject(), () -> {
            try {
                //TODO add a prepend functionality
                I18NFileContext i18nFile = new I18NFileContext(ctx.getProject(), ctx.getPsiFile());
                I18NJsonDeclFileTransformation transformation = new I18NJsonDeclFileTransformation(i18nFile, I18N_MARKER, "PLEASE DO NOT DELETE THIS VALUE, IT MARKS THE FILE  AS I18N JSON FILE");
                i18nFile.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());

                i18nFile.commit();
                i18nFile.reformat();
                IntellijUtils.showInfoMessage("i18n marker information has been added", "Marker has been added");
            } catch (IOException ex) {
                IntellijUtils.handleEx(ctx.getProject(), ex);
            }
        }));
    }


}
