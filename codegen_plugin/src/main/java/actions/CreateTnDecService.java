package actions;

import actions_all.shared.GenerateFileAndAddRef;
import actions_all.shared.Messages;
import actions_all.shared.SimpleFileNameTransformer;
import actions_all.shared.VisibleAssertions;
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
import factories.TnDecGroupFactory;
import gui.CreateService;
import gui.support.DialogWrapperCreator;
import org.jetbrains.annotations.NotNull;
import supportive.dtos.ModuleElementScope;
import supportive.utils.IntellijUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static actions_all.shared.FormAssertions.*;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecService extends AnAction {


    public CreateTnDecService() {
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


        final gui.CreateService mainForm = new gui.CreateService();

        DialogWrapper dialogWrapper = new DialogWrapperCreator(project, mainForm.getMainPanel())
                .withDimensionKey("AnnService").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();


        dialogWrapper.setTitle("Create Service");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 150));

        dialogWrapper.setResizable(false);

        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            ControllerJson model = new ControllerJson((String) mainForm.getTxtName().getValue(), "", "");
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
            supportive.utils.IntellijUtils.showInfoMessage("The Service has been generated", "Info");
        }
    }

    @NotNull
    private ValidationInfo[] validateInput(CreateService mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty((String) mainForm.getTxtName().getValue(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                assertPattern((String) mainForm.getTxtName().getValue(), VALID_NAME, Messages.ERR_SERVICE_PATTERN, mainForm.getTxtName())};
    }

    void buildFile(Project project, ControllerJson model, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = model.getName();

            FileTemplate vslTemplate = getJ2eeTemplate(project);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("NAME", className);

            generate(project, folder, className, vslTemplate, attrs);
        });
    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), ModuleElementScope.PROVIDERS).run();
    }

    protected FileTemplate getJ2eeTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_SERVICE);
    }
}
