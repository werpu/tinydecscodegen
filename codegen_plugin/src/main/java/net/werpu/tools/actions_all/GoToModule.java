package net.werpu.tools.actions_all;

import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.TypescriptFileContext;

import java.util.List;

import static net.werpu.tools.supportive.reflectRefact.IntellijRefactor.NG_MODULE;

/**
 * Got to the parent module definition navigational handler
 */
public class GoToModule extends AnAction {

    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tsOnlyVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        TypescriptFileContext tsContext = new TypescriptFileContext(anActionEvent);

        List<IntellijFileContext> annotatedModules = tsContext.findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));
        if (annotatedModules.isEmpty()) {
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("No parent module could be found", "Info");
        }
        FileEditorManager.getInstance(tsContext.getProject()).openFile(annotatedModules.get(0).getVirtualFile(), true);
    }
}