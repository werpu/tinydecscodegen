package net.werpu.tools.actions;

import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.Messages;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import dtos.ControllerJson;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.gui.CreateTnDecComponent;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.StringUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.*;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecRun extends AnAction {


    public CreateTnDecRun() {
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);


        VirtualFile folder = IntellijUtils.getFolderOrFile(event);


        final net.werpu.tools.gui.CreateTnDecComponent mainForm = new net.werpu.tools.gui.CreateTnDecComponent();
        mainForm.getLblSelector().setText("Run class name *");
        mainForm.getLblTitle().setText("Create an Annotated Run Config");
        mainForm.getLblTemplate().setVisible(false);
        mainForm.getLblControllerAs().setVisible(false);

        mainForm.getTxtTemplate().setVisible(false);
        mainForm.getTxtControllerAs().setVisible(false);

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.rootPanel)
                .withDimensionKey("AnnRun").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();


        dialogWrapper.setTitle("Create Run Service");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            ControllerJson model = new ControllerJson(mainForm.getName(), mainForm.getTemplate(), mainForm.getControllerAs());
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("The Run Config has been generated", "Info");
        }
    }

    @NotNull
    private ValidationInfo[] validateInput(CreateTnDecComponent mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                assertPattern(mainForm.getName(), MODULE_PATTERN, Messages.ERR_RUN_PATTERN, mainForm.getTxtName())};
    }

    void buildFile(Project project, ControllerJson model, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String name = model.getName();
            String className = StringUtils.toCamelCase(name);


            FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_RUN);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("CLASS_NAME", className);
            attrs.put("NAME", className);

            generate(project, folder, className, vslTemplate, attrs);
        });
    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), ModuleElementScope.DECLARATIONS).run();
    }
}
