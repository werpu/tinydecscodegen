package net.werpu.tools.actions;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiL18nEntryContext;
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
