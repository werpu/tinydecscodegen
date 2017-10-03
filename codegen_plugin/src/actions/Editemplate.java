package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import utils.IntellijRefactor;
import utils.IntellijUtils;

public class Editemplate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        // TODO: insert action logic here
        final Project project = IntellijUtils.getProject(event);
        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        IntellijRefactor.findTemplate(PsiManager.getInstance(project).findFile(folder));
    }
}
