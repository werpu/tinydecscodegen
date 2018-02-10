package actions_all;

import actions_all.shared.JavaFileContext;
import actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;

public class GoToJava extends AnAction {

    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tsOnlyVisible(anActionEvent);
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
