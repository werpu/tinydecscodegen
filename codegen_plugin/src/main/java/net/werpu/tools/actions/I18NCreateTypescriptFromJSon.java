package net.werpu.tools.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.indexes.L18NIndexer;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
        diffOrWriteGenericFile(ctx.getProject(), ctx.getVirtualFile().getParent(), newFileName, "booga", getTypescriptLanguageDef());

    }
}
