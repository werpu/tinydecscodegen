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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiI18nEntryContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.refactor.DummyInsertPsiElement;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.JS_VAR_STATEMENT;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.DIRECT_CHILD;
import static net.werpu.tools.supportive.reflectRefact.navigation.TreeQueryEngine.PARENTS_EQ_FIRST;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.invokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;

/**
 * Helper action to mark a specific file as i18n file
 * the idea is to provide a user action from the editor which allows
 * to mark a file which has the correct patterns as i18n file
 */
public class MarkAsI18NTSFile extends AnAction {
    public static final String I18N_MARKER = "@def: i18nfile";

    public MarkAsI18NTSFile() {
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (!IntellijUtils.isTypescript(ctx.getVirtualFile().getFileType())) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        //now we need to query the structure of the file for a typescript variable
        //under root and the associcated map

        assertI18nPattern(anActionEvent, ctx);
    }

    protected void assertI18nPattern(@NotNull AnActionEvent anActionEvent, IntellijFileContext ctx) {
        try {
            PsiI18nEntryContext elCtx = new PsiI18nEntryContext(new PsiElementContext(ctx.getPsiFile()));

            boolean correctPattern = elCtx.getRootTreeReference() != null;
            boolean noRef = !elCtx.getText().contains(I18N_MARKER);
            anActionEvent.getPresentation().setEnabledAndVisible(correctPattern && noRef);
        } catch (Throwable t) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TypescriptFileContext ctx = new TypescriptFileContext(e);

        PsiI18nEntryContext elCtx = new PsiI18nEntryContext(new PsiElementContext(ctx.getPsiFile()));

        addMarker(ctx, elCtx);
    }

    protected void addMarker(TypescriptFileContext ctx, PsiI18nEntryContext elCtx) {
        Optional<PsiElementContext> foundElement = elCtx.getExportVar().$q(PARENTS_EQ_FIRST(JS_VAR_STATEMENT), DIRECT_CHILD(PsiWalkFunctions.JS_DOC_COMMENT)).findFirst();
        invokeLater(() -> writeTransaction(ctx.getProject(), () -> {
            try {
                updateInsertMarker(ctx, elCtx, foundElement);
                IntellijUtils.showInfoMessage("i18n marker information has been added", "Marker has been added");
            } catch (IOException ex) {
                IntellijUtils.handleEx(ctx.getProject(), ex);
            }
        }));
    }

    protected void updateInsertMarker(TypescriptFileContext ctx, PsiI18nEntryContext elCtx, Optional<PsiElementContext> foundElement) throws IOException {

        if (!foundElement.isPresent()) {
            //We add a comment to the current editor before the var def
            PsiElementContext varDcl = elCtx.getExportVar().$q(PARENTS_EQ_FIRST(JS_VAR_STATEMENT)).findFirst().get();
            ctx.addRefactoring(new RefactorUnit(ctx.getPsiFile(), new DummyInsertPsiElement(varDcl.getTextRangeOffset()), "/** \n* " + I18N_MARKER + " \n*/\n"));
            ctx.commit();

        } else {
            //we simply add the ref to the existing comment in a new line
            PsiElementContext commentSection = foundElement.get();
            ctx.addRefactoring(new RefactorUnit(ctx.getPsiFile(), new DummyInsertPsiElement(commentSection.getTextRangeOffset() + commentSection.getTextLength() - 2), " * " + I18N_MARKER + " \n"));
            ctx.commit();
        }
    }
}
