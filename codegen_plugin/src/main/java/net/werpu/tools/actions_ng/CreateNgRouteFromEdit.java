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

import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.supportive.fs.common.ComponentFileContext;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class CreateNgRouteFromEdit extends CreateNgRoute {
    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        if (!anActionEvent.getPresentation().isEnabledAndVisible()) {
            return;
        }

        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (VisibleAssertions.assertNotTs(ctx) ||
                !VisibleAssertions.assertController(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);

    }

    @NotNull
    public Optional<ComponentFileContext> getDefaultComponentData(IntellijFileContext fileContext) {
        ComponentFileContext compCtx = new ComponentFileContext(fileContext);
        return ofNullable(compCtx);
    }
}
