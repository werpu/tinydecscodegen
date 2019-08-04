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

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotTs;
import static net.werpu.tools.supportive.utils.IntellijUtils.getFolderOrFile;
import static net.werpu.tools.supportive.utils.IntellijUtils.getJsonLanguageDef;

public class I18NCreateJsonFromTypescript  extends I18NCreateTypescriptFromJSon {

    @Override
    public void update(AnActionEvent anActionEvent) {

        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (getFolderOrFile(anActionEvent) == null ||
                ctx.getVirtualFile().isDirectory() || assertNotTs(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Optional<IntellijFileContext> found =  L18NIndexer.getAllAffectedFiles(anActionEvent.getProject())
                .stream().filter(fileContext -> normalize(fileContext).equals(normalize(ctx))).findFirst();
        anActionEvent.getPresentation().setEnabledAndVisible(found.isPresent());
    }

    @NotNull
    public String normalize(IntellijFileContext fileContext) {
        String path = fileContext.getVirtualFile().getPath();
        path = path.substring(0, path.lastIndexOf("."));
        return path;
    }


    public Language getLanguageDef() {
        return getJsonLanguageDef();
    }

    @NotNull
    public String getTemplate() {
        return TnDecGroupFactory.TPL_I18N_JSON_FILE;
    }

    @NotNull
    public String calculateFileName(IntellijFileContext ctx) {
        return ctx.getVirtualFile().getName().replaceAll("\\.ts$", ".json");
    }


}
