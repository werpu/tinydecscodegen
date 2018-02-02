package actions;

import actions.shared.JavaFileContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import utils.IntellijFileContext;

import static actions.shared.VisibleAssertions.assertNotTs;

public class GoToJava extends AnAction {

    public void update(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (assertNotTs(ctx) || assertNotTs(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        JavaFileContext javaFileContext = new JavaFileContext(anActionEvent);
        if(javaFileContext.isError()) {
            return;
        }
        FileEditorManager.getInstance(javaFileContext.getProject()).openFile(javaFileContext.getJavaFile().getVirtualFile(), true);
    }
}
