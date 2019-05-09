package net.werpu.tools.actions_all;


import com.intellij.openapi.actionSystem.AnActionEvent;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.L18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiL18nEntryContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;
import net.werpu.tools.supportive.refactor.RefactorUnit;
import net.werpu.tools.supportive.transformations.L18NDeclFileTransformation;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.werpu.tools.supportive.utils.IntellijRunUtils.invokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.writeTransaction;

/**
 * similar marker on the json side compared to the
 * Typescript side
 *
 */
public class MarkAsI18NJsonFile extends MarkAsI18NTSFile {
    //we inherit a lot of functionality only the visibility scope and the refactoring is different

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);

        if (!IntellijUtils.isJSON(ctx.getVirtualFile().getFileType())) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        //funnily we can recycle the i18n file pattern detection
        assertI18nPattern(anActionEvent, ctx);
    }


    protected void addMarker(TypescriptFileContext ctx,final PsiL18nEntryContext elCtx) {

        invokeLater(() -> writeTransaction(ctx.getProject(), () -> {
            try {
                //TODO add a prepend functionality
                L18NFileContext i18nFile = new L18NFileContext(ctx.getProject(), ctx.getPsiFile());
                L18NDeclFileTransformation transformation = new L18NDeclFileTransformation(i18nFile, I18N_MARKER, "PLEASE DO NOT DELETE THIS VALUE, IT MARKS THE FILE  AS I18N JSON FILE");
                i18nFile.addRefactoring((RefactorUnit) transformation.getTnDecRefactoring());

                i18nFile.commit();
                i18nFile.reformat();
                IntellijUtils.showInfoMessage("i18n marker information has been added", "Marker has been added");
            } catch (IOException ex) {
                IntellijUtils.handleEx(ctx.getProject(), ex);
            }
        }));
    }


}
