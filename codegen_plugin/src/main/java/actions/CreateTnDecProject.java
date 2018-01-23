package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.IntellijResourceDir;
import utils.IntellijUtils;
import utils.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.intellij.openapi.ui.Messages;

import static actions.FormAssertions.assertNotNullOrEmpty;

public class CreateTnDecProject extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Project project = IntellijUtils.getProject(anActionEvent);

        createDialog(project);


    }

    private void createDialog(Project project) {
        final gui.CreateTnProject mainForm = new gui.CreateTnProject();
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

            @Override
            public void init() {
                super.init();
            }

            @Nullable
            @NotNull
            protected List<ValidationInfo> doValidateAll() {
                return Arrays.asList(
                        assertNotNullOrEmpty(mainForm.projectDir.getText(), actions.Messages.ERR_PROJECT_DIR_CHOSEN, mainForm.projectDir),
                        assertNotNullOrEmpty(mainForm.targetDir.getText(), actions.Messages.ERR_TARGET_DIR_CHOSEN, mainForm.targetDir))
                        .stream().filter(s -> s != null).collect(Collectors.toList());
            }

            public void show() {

                this.init();
                this.setModal(true);
                this.pack();
                super.show();
            }
        };

        mainForm.setProject(project);


        dialogWrapper.setTitle("Create Project");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        dialogWrapper.show();

        if (dialogWrapper.isOK()) {
            try {
                boolean targetPresent = Files.list(Paths.get(mainForm.projectDir.getText())).findAny().isPresent();
                if(targetPresent) {
                    int result = Messages.showYesNoDialog("The project target directory already contains files some of the files might be overwritten, do you like to proceed?", "Overwrite Warning", null);
                    if(result == Messages.NO) {
                        PopupUtil.showBalloonForActiveFrame("Project generation was cancelled", MessageType.INFO);
                        return;
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            IntellijResourceDir resources = new IntellijResourceDir(getResourcePath());



            resources.copyTo(new File(mainForm.projectDir.getText()), new TextTransformer() {
                @Override
                public String transform(String out, String data) {
                    String relPath = Paths.get(out).relativize(Paths.get(mainForm.targetDir.getText().replaceAll("\\\\","/"))).toString();
                    String projRelPath = Paths.get(out).relativize(Paths.get(mainForm.projectDir.getText().replaceAll("\\\\","/"))).toString();
                    data = data.replaceAll("\\$\\{deployment_root_rel\\}", relPath);
                    data = data.replaceAll("\\$\\{proj_root_rel\\}", projRelPath);

                    return data;
                }
            });
            PopupUtil.showBalloonForActiveFrame("The project as been generated, please run npm install to load all needed dependencies", MessageType.INFO);
        }

    }

    @NotNull
    protected String getResourcePath() {
        return "/resources/projectLayout/tnDec";
    }
}