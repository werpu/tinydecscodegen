package net.werpu.tools.actions;

import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotJson;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.JSON_OBJECT;
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
                ctx.getVirtualFile().isDirectory()) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        if(assertNotJson(ctx)) {
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
        String newFileName = ctx.getVirtualFile().getName().replaceAll("\\.json$", ".ts");

        //TODO add transformation hooks here

        PsiL18nEntryContext entry = new PsiL18nEntryContext(new L18NFileContext(e).getResourceRoot());

        FileTemplate vslTemplate = FileTemplateManager.getInstance(ctx.getProject()).getJ2eeTemplate(TnDecGroupFactory.TPL_I18N_TS_FILE);
        Map<String, Object> attrs = Maps.newHashMap();

        attrs.put("ROOT_ELEMENT", entry.getRootTreeReference());
        attrs.put("EXPORT_VAR", "translation");

        try {
            String vslTemplateText = vslTemplate.getText();
            String mergedContent = FileTemplateUtil.mergeTemplate(attrs, vslTemplateText, false);
            PsiFile file = createRamFileFromText(ctx.getProject(),ctx.getVirtualFile().getName(), mergedContent, getTypescriptLanguageDef());
            PsiElement reformatted = CodeStyleManager.getInstance(ctx.getProject()).reformat(file.getOriginalElement().getChildren()[0]);

            diffOrWriteGenericFile(ctx.getProject(), ctx.getVirtualFile().getParent(), newFileName, reformatted.getText(), getTypescriptLanguageDef());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
}
