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

package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.NG_PRJ_MARKER;
import static net.werpu.tools.supportive.utils.IntellijUtils.*;

public class MarkProjectAsNg extends AnAction {
    @Override
    public void update(AnActionEvent anActionEvent) {
        try {
            IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
            if (ctx.isAngularChild(AngularVersion.NG)) {
                anActionEvent.getPresentation().setEnabledAndVisible(false);
                return;
            }

            if (ctx.isAngularChild(AngularVersion.TN_DEC)) {
                anActionEvent.getPresentation().setEnabledAndVisible(false);
                return;
            }
            anActionEvent.getPresentation().setEnabledAndVisible(ctx.getVirtualFile().isDirectory());
        } catch (Throwable t) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
        }

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        IntellijFileContext ctx = new IntellijFileContext(e);
        VirtualFile parFolder = ctx.getVirtualFile().isDirectory() ? ctx.getVirtualFile() : ctx.getVirtualFile().getParent();

        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                create(ctx.getProject(), parFolder, "", NG_PRJ_MARKER);
                refresh();
                ApplicationManager.getApplication().invokeLater(() -> {
                    refresh();
                    showInfoMessage("The folder now is an angular project", "Info");
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                showErrorDialog(ctx.getProject(), "An error has occurred during project marking check your error log for more details", "Error in project marking");
            }
        });

    }

}
