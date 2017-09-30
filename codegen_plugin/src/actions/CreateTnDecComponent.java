package actions;

import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.FileTemplateBase;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dtos.ComponentJson;
import factories.TnDecGroupFactory;
import utils.IntellijUtils;
import utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecComponent extends AnAction implements DumbAware {


    public CreateTnDecComponent() {
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }

   /* @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("Ts").addKind("Java File", JavaFileType.INSTANCE.getIcon(), "Java File")
                .addKind("Java File2", JavaFileType.INSTANCE.getIcon(), "Java File");
        ;

    }*/

    //@Override
    //protected String getActionName(PsiDirectory psiDirectory, String s, String s1) {
    //    return null;
    //}

    //https://intellij-support.jetbrains.com/hc/en-us/community/posts/206755515-CreateFileFromTemplateDialog-with-additional-input-fields
    //@Override

    /**
     * decided whether the menu item should be shown
     */
    /*public void update(AnActionEvent event) {
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
*/
    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);



            VirtualFile file = (VirtualFile) event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
            VirtualFile folder = file.getParent();


        JDialog dataDialog = new JDialog();

        gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();
        dataDialog.setContentPane(mainForm.rootPanel);
        dataDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dataDialog.pack();
        dataDialog.setPreferredSize(new Dimension(700, 700));
        SwingUtils.centerOnParent(dataDialog, true);

        dataDialog.setVisible(true);

        mainForm.onCancel(model -> {
            dataDialog.dispose();
            return true;
        });

        mainForm.onOk(model -> {
            dataDialog.dispose();
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
            return true;
        });



    }

    void buildFile(Project project, ComponentJson model, VirtualFile folder) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            String className = toCamelCase(model.getSelector());

            FileTemplate vslTemplate = (FileTemplateBase)
                    FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("SELECTOR", model.getSelector());
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());

            try {
                String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
                String fileNmae = className + ".ts";
                IntellijUtils.createAndOpen(project, folder, str, fileNmae);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    //https://stackoverflow.com/questions/1086123/string-conversion-to-title-case
    public static String toCamelCase(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replaceAll("-","");
    }

}
