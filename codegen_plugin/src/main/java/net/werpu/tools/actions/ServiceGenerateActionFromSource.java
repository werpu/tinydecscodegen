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
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import lombok.CustomLog;
import net.werpu.tools.actions_all.shared.JavaFileContext;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.supportive.fs.common.AngularVersion;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.*;

@CustomLog
public class ServiceGenerateActionFromSource extends AnAction {
    @Override
    public void update(AnActionEvent anActionEvent) {

        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (!VisibleAssertions.hasAngularVersion(anActionEvent, AngularVersion.TN_DEC) ||
                assertNotJava(ctx) || (assertNotJavaRest(ctx) && assertNotSpringRest(ctx))) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        if (event.getData(PlatformDataKeys.EDITOR) == null) {
            net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(event.getProject(), "Error", "No editor found, please focus on an open source file");
            return;
        }

        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.fileNameTransformer = new SimpleFileNameTransformer();
            IntellijUtils.generateService(javaData.getProject(), javaData.getModule(), (PsiJavaFile) javaData.getJavaFile(), false);
        } catch (RuntimeException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), net.werpu.tools.actions_all.shared.Messages.ERR_OCCURRED);
        }

    }
}

