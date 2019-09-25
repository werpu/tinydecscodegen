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
import com.intellij.openapi.fileEditor.FileEditorManager;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;

import java.util.List;

import static net.werpu.tools.supportive.reflectRefact.IntellijRefactor.NG_MODULE;

/**
 * Got to the parent module definition navigational handler
 */
public class GoToModule extends AnAction {
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tsOnlyVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        TypescriptFileContext tsContext = new TypescriptFileContext(anActionEvent);

        List<IntellijFileContext> annotatedModules = tsContext.findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));
        if (annotatedModules.isEmpty()) {
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("No parent module could be found", "Info");
        }
        FileEditorManager.getInstance(tsContext.getProject()).openFile(annotatedModules.get(0).getVirtualFile(), true);
    }
}
