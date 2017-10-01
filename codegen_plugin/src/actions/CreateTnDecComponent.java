package actions;

import actions.shared.GenerateFileAndAddRef;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import dtos.ComponentJson;
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
public class CreateTnDecComponent extends AnAction implements DumbAware {


    public CreateTnDecComponent() {
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);


        VirtualFile file = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        VirtualFile folder = file;


        final gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();

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
                    ValidationInfo info = new ValidationInfo("Tag selector  must have a value", mainForm.getTxtName());
                    return info;
                }

                if (!Strings.isNullOrEmpty(mainForm.getName()) && Strings.isNullOrEmpty(mainForm.getControllerAs())) {
                    ValidationInfo info = new ValidationInfo("Controller As  must have a value", mainForm.getTxtControllerAs());
                    return info;
                }

                if (Strings.isNullOrEmpty(mainForm.getName()) || Strings.isNullOrEmpty(mainForm.getControllerAs())) {
                    ValidationInfo info = new ValidationInfo("Tag selector and Controller As must have values");
                    return info;
                }
                if (!(mainForm.getName().matches("[0-9a-z\\-]+"))) {
                    ValidationInfo info = new ValidationInfo("The tag selector must consist of lowercase letters numbers or '-' ", mainForm.getTxtName());
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

        dialogWrapper.setTitle("Create Component");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            ComponentJson model = new ComponentJson(mainForm.getName(), mainForm.getTemplate(), mainForm.getControllerAs());
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
        }
    }

    void buildFile(Project project, ComponentJson model, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = IntellijUtils.toCamelCase(model.getSelector());

            FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("SELECTOR", model.getSelector());
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());

            new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, ModuleElementScope.EXPORT).run();

        });


    }


}
