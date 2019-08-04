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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.werpu.tools.actions_all.shared.JavaFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.util.Collection;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotJava;

public class GoToTs extends AnAction {

    public void update(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (assertNotJava(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        JavaFileContext javaFileContext = new JavaFileContext(anActionEvent);
        Collection<PsiFile> refs = IntellijUtils.searchRefs(javaFileContext.getProject(), javaFileContext.getClassName(), "ts");
        if (refs.size() == 0) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }


        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        JavaFileContext javaFileContext = new JavaFileContext(anActionEvent);
        Collection<PsiFile> refs = IntellijUtils.searchRefs(javaFileContext.getProject(), javaFileContext.getClassName(), "ts");


        //final FileEditorManagerEx edManager = (FileEditorManagerEx) FileEditorManagerEx.getInstance(javaFileContext.getProject());

        //EditorWindow currentWindow = edManager.getCurrentWindow();
        //edManager.createSplitter(SwingConstants.NEXT, currentWindow);
        refs.stream().forEach(ref -> {
            final VirtualFile virtualFile = refs.iterator().next().getVirtualFile();
            FileEditorManager.getInstance(javaFileContext.getProject()).openFile(virtualFile, true);
        });
    }
}
