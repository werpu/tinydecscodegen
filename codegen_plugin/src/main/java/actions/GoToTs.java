package actions;

import actions.shared.JavaFileContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import utils.fs.IntellijFileContext;
import utils.IntellijUtils;

import java.util.Collection;

import static actions.shared.VisibleAssertions.assertNotJava;

public class GoToTs extends AnAction {

    public void update(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (assertNotJava(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        JavaFileContext javaFileContext = new JavaFileContext(anActionEvent);
        Collection<PsiFile> refs = IntellijUtils.searchRefs(javaFileContext.getProject(), javaFileContext.getClassName(), "ts");
        if (refs.size() == 0) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }


        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        JavaFileContext javaFileContext = new JavaFileContext(anActionEvent);
        Collection<PsiFile> refs = IntellijUtils.searchRefs(javaFileContext.getProject(), javaFileContext.getClassName(), "ts");


        //final FileEditorManagerEx edManager = (FileEditorManagerEx) FileEditorManagerEx.getInstance(javaFileContext.getProject());

        //EditorWindow currentWindow = edManager.getCurrentWindow();
        //edManager.createSplitter(SwingConstants.NEXT, currentWindow);
        refs.stream().forEach(ref -> {
            final VirtualFile virtualFile = refs.iterator().next().getVirtualFile();
            FileEditorManager.getInstance(javaFileContext.getProject()).openFile(virtualFile, true);
        });
    }
}
