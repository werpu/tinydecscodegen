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

package net.werpu.tools.actions_ng;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import lombok.CustomLog;
import net.werpu.tools.actions.ServiceGenerationAction;
import net.werpu.tools.actions_all.shared.JavaFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;

import java.io.IOException;

@CustomLog
public class ServiceGenerateActionWithoutCompile extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {

        final JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;

        try {
            IntellijUtils.generateService(javaData.getProject(), javaData.getModule(), javaData.getClassName(), javaData.getJavaFile(), javaData.getClassLoader(), true);
        } catch (RuntimeException | IOException | ClassNotFoundException e) {
            log.error(e);
            Messages.showErrorDialog(javaData.getProject(), e.getMessage(), net.werpu.tools.actions_all.shared.Messages.ERR_OCCURRED);
        }

    }
}

