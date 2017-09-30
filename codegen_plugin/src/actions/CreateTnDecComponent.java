package actions;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CreateTnDecComponent extends CreateFileFromTemplateAction implements DumbAware {


    public CreateTnDecComponent() {
        super("TDecs Angular Component", "Creates a Tiny Decorations Angular Component", null);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("Ts").addKind("Java File", JavaFileType.INSTANCE.getIcon(), "Java File")
                .addKind("Java File2", JavaFileType.INSTANCE.getIcon(), "Java File");
        ;

    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s, String s1) {
        return null;
    }

    //https://intellij-support.jetbrains.com/hc/en-us/community/posts/206755515-CreateFileFromTemplateDialog-with-additional-input-fields
    @Override
    /**
     * decided whether the menu item should be shown
     */
    public void update(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();

        boolean enabled = true;//whatever criteria you have for when it's visible/enabled
        Presentation presentation = event.getPresentation();
        presentation.setVisible(enabled);
        presentation.setEnabled(enabled);
    }

    @Override
    public void beforeActionPerformedUpdate(@NotNull AnActionEvent e) {
        super.beforeActionPerformedUpdate(e);
    }



}
