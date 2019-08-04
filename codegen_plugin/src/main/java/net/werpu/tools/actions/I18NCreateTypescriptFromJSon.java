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

import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiI18nEntryContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotJson;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;
import static net.werpu.tools.supportive.utils.IntellijUtils.*;

/**
 * json->typescript i18n conversion (we will add a refactoring for the templates later
 * for now it is just a simple conversion)
 *
 */
public class I18NCreateTypescriptFromJSon extends AnAction {

    @Override
    public void update(AnActionEvent anActionEvent) {

        VisibleAssertions.templateVisible(anActionEvent);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (getFolderOrFile(anActionEvent) == null ||
                ctx.getVirtualFile().isDirectory() || assertNotJson(ctx) ) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }


        Optional<IntellijFileContext> found =  L18NIndexer.getAllAffectedFiles(anActionEvent.getProject())
                .parallelStream().filter(fileContext -> fileContext.getVirtualFile().getPath().equals(ctx.getVirtualFile().getPath())).findFirst();
        anActionEvent.getPresentation().setEnabledAndVisible(found.isPresent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        //map the typescript file into a similarily named json file in the same dir
        //and if the file exists already open a diff dialog instead of just creating it

        IntellijFileContext ctx = new IntellijFileContext(e);
        //first calculate the new filename
        String newFileName = calculateFileName(ctx);

        //TODO add transformation hooks here

        PsiI18nEntryContext entry = getResourceRoot(e);

        FileTemplate vslTemplate = FileTemplateManager.getInstance(ctx.getProject()).getJ2eeTemplate(getTemplate());
        Map<String, Object> attrs = Maps.newHashMap();

        attrs.put("ROOT_ELEMENT", entry.getRootTreeReference());
        attrs.put("EXPORT_VAR", "translation");

        try {
            String vslTemplateText = vslTemplate.getText();
            String mergedContent = FileTemplateUtil.mergeTemplate(attrs, vslTemplateText, false);
            PsiFile file = createRamFileFromText(ctx.getProject(),ctx.getVirtualFile().getName(), mergedContent, getLanguageDef());
            writeTransaction(ctx.getProject(), () -> {
                PsiElement reformatted = CodeStyleManager.getInstance(ctx.getProject()).reformat(file.getOriginalElement().getChildren()[0]);
                diffOrWriteGenericFile(ctx.getProject(), ctx.getVirtualFile().getParent(), newFileName, reformatted.getText(), getLanguageDef());
            });

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @NotNull
    public PsiI18nEntryContext getResourceRoot(@NotNull AnActionEvent e) {
        return new PsiI18nEntryContext(new PsiElementContext(new IntellijFileContext(e).getPsiFile()));
    }

    public Language getLanguageDef() {
        return getTypescriptLanguageDef();
    }

    @NotNull
    public String getTemplate() {
        return TnDecGroupFactory.TPL_I18N_TS_FILE;
    }

    @NotNull
    public String calculateFileName(IntellijFileContext ctx) {
        return ctx.getVirtualFile().getName().replaceAll("\\.json$", ".ts");
    }
}
