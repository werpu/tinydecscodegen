package actions;

import actions.shared.GenerateFileAndAddRef;
import actions.shared.SimpleFileNameTransformer;
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
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.vfs.VirtualFile;
import configuration.ConfigSerializer;
import dtos.ControllerJson;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.IntellijUtils;
import utils.ModuleElementScope;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static actions.FormAssertions.*;

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
        mainForm.getLblExport().setText("Export Module");
        mainForm.getLblCreateDir().setVisible(true);
        mainForm.getLblCreateStructue().setVisible(true);
        mainForm.getCbCreateDir().setVisible(true);
        mainForm.getCbCreateStructure().setVisible(true);
        mainForm.getTxtTemplate().setVisible(false);
        mainForm.getPnEditorHolder().setVisible(false);

        mainForm.getCbCreateStructure().setSelected(ConfigSerializer.getInstance().getState().isModuleGenerateStructure());
        mainForm.getCbExport().setSelected(ConfigSerializer.getInstance().getState().isModuleExport());
        mainForm.getCbCreateDir().setSelected(ConfigSerializer.getInstance().getState().isModuleGenerateFolder());

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
            @NotNull
            protected List<ValidationInfo> doValidateAll() {
                return Arrays.asList(
                        assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                        assertPattern(mainForm.getName(), VALID_NAME, Messages.ERR_MODULE_PATTERN, mainForm.getTxtName())
                ).stream().filter(s -> s != null).collect(Collectors.toList());
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


        dialogWrapper.setTitle("Create Module");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            boolean generateFolder = mainForm.getCbCreateDir().isSelected();
            boolean generateStructure = mainForm.getCbCreateStructure().isSelected();

            ControllerJson model = new ControllerJson(mainForm.getName(), mainForm.getTemplate(), mainForm.getControllerAs());
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder, generateFolder, generateStructure));
            PopupUtil.showBalloonForActiveFrame("The Module has been generated", MessageType.INFO);

            ConfigSerializer.getInstance().getState().setModuleExport(mainForm.getCbExport().isSelected());
            ConfigSerializer.getInstance().getState().setModuleGenerateFolder(mainForm.getCbCreateDir().isSelected());
            ConfigSerializer.getInstance().getState().setModuleGenerateStructure(mainForm.getCbCreateStructure().isSelected());
        }
    }

    void buildFile(Project project, ControllerJson model, final VirtualFile folder, boolean createDir, boolean createStructure) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            VirtualFile finalFolder = folder;
            String name = model.getName();
            String className = IntellijUtils.toCamelCase(name);
            if (createDir) {

                try {
                    finalFolder = folder.createChildDirectory(project, getModuleName(className));

                } catch (IOException e) {
                    IntellijUtils.handleEx(e);
                    return;
                }
            }

            if (createStructure) {
                try {
                    finalFolder.createChildDirectory(project, "dtos");
                    finalFolder.createChildDirectory(project, "pages");
                    finalFolder.createChildDirectory(project, "services");
                    finalFolder.createChildDirectory(project, "components");
                    finalFolder.createChildDirectory(project, "shared");
                } catch (IOException e) {
                    IntellijUtils.handleEx(e);
                    return;
                }

            }

            FileTemplate vslTemplate = getJ2eeTemplate(project);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("CLASS_NAME", className);
            attrs.put("NAME", name);


            generate(project, finalFolder, className, vslTemplate, attrs);
        });
    }

    protected String getModuleName(String className) {
        return className.toLowerCase().replaceAll("module$", "");
    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), ModuleElementScope.IMPORT).run();
    }

    protected FileTemplate getJ2eeTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_MODULE);
    }
}
