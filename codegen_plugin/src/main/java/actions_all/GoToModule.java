package actions_all;

import actions_all.shared.JavaFileContext;
import actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiFile;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.TypescriptFileContext;
import supportive.utils.IntellijUtils;

import java.util.Collection;
import java.util.List;

import static actions_all.shared.VisibleAssertions.assertNotJava;
import static supportive.reflectRefact.IntellijRefactor.NG_MODULE;

/**
 * Got to the parent module definition navigational handler
 *
 */
public class GoToModule extends AnAction {

    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tsOnlyVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        TypescriptFileContext tsContext = new TypescriptFileContext(anActionEvent);

        List<IntellijFileContext> annotatedModules = tsContext.findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));
        if(annotatedModules.isEmpty()) {
            PopupUtil.showBalloonForActiveFrame("No parent module could be found", MessageType.INFO);
        }
        FileEditorManager.getInstance(tsContext.getProject()).openFile(annotatedModules.get(0).getVirtualFile(), true);
    }
}
