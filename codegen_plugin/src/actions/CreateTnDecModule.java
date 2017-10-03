package actions;

import actions.shared.GenerateFileAndAddRef;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import dtos.ControllerJson;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.Nullable;
import utils.IntellijUtils;
import utils.ModuleElementScope;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecModule extends AnAction implements DumbAware {


    public CreateTnDecModule() {
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);


        VirtualFile folder = IntellijUtils.getFolderOrFile(event);


        final gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();
        mainForm.getLblSelector().setText("Module name *");
        mainForm.getLblTitle().setText("Create an Annotated Module");
        mainForm.getLblTemplate().setVisible(false);
        mainForm.getLblControllerAs().setVisible(false);

        mainForm.getTxtTemplate().setVisible(false);
        mainForm.getTxtControllerAs().setVisible(false);

        DialogWrapper dialogWrapper = new DialogWrapper(project, true, DialogWrapper.IdeModalityType.PROJECT) {

            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                return mainForm.rootPanel;
            }

            @Nullable
            @Override
            protected String getDimensionServiceKey() {
                return "AnnComponent";
            }

            @Nullable
            @Override
            protected ValidationInfo doValidate() {

                if (Strings.isNullOrEmpty(mainForm.getName()) && !Strings.isNullOrEmpty(mainForm.getControllerAs())) {
                    ValidationInfo info = new ValidationInfo("Name  must have a value", mainForm.getTxtName());
                    return info;
                }

                if (!(mainForm.getName().matches("[0-9A-Za-z\\.]+"))) {
                    ValidationInfo info = new ValidationInfo("Module name must consist of letters . or numbers", mainForm.getTxtName());
                    return info;
                }

                return null;
            }

            @Override
            public void init() {
                super.init();
            }

            public void show() {

                this.init();
                this.setModal(true);
                this.pack();
                super.show();
            }
        };

        dialogWrapper.setTitle("Create Service");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            ControllerJson model = new ControllerJson(mainForm.getName(), mainForm.getTemplate(), mainForm.getControllerAs());
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
        }
    }

    void buildFile(Project project, ControllerJson model, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String name = model.getName();
            String className = IntellijUtils.toCamelCase(name);


            FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_MODULE);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("CLASS_NAME", className);
            attrs.put("NAME", name);

            new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, ModuleElementScope.IMPORT).run();
        });
    }
}
