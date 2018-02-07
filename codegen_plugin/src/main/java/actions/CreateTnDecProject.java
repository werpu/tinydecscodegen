package actions;

import com.google.common.collect.Maps;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.IntellijResourceDir;
import supportive.utils.IntellijUtils;
import supportive.fs.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.intellij.openapi.ui.Messages;

import static actions.shared.FormAssertions.assertNotNullOrEmpty;

public class CreateTnDecProject extends AnAction implements DumbAware {

    public static final String TN_PROJECT_LAYOUT = "/resources/projectLayout/tnDec";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Project project = IntellijUtils.getProject(anActionEvent);

        VirtualFile file = (VirtualFile) anActionEvent.getDataContext().getData(DataKeys.VIRTUAL_FILE);
        //VirtualFile folder = file.getParent();



        String path = file.isDirectory() ? file.getPath() : file.getParent().getPath();

        String outputPath = path+"/../target";
        Path output = Paths.get(outputPath);
        output = output.normalize();

        createDialog(project, path, output.toFile().getPath());


    }

    private void createDialog(Project project,String projectFolder, String targetFolder) {
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
        mainForm.projectDir.setText(projectFolder);
        mainForm.targetDir.setText(targetFolder);

        dialogWrapper.setTitle("Create Project");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        dialogWrapper.show();

        if (dialogWrapper.isOK()) {
            final String projectDir = mainForm.projectDir.getText();

            try {
                boolean targetPresent = Files.list(Paths.get(projectDir)).findAny().isPresent();
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

            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    FileDocumentManager.getInstance().saveAllDocuments();
                    ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
                    IntellijResourceDir resources = new IntellijResourceDir(getResourcePath(), getSubPath());


                    resources.copyTo(new File(projectDir), new TextTransformer() {
                        @Override
                        public String transform(String out, String data) {
                            String relPath = Paths.get(out).relativize(Paths.get(mainForm.targetDir.getText().replaceAll("\\\\", "/"))).toString();
                            relPath = relPath.replaceAll("\\\\", "/");
                            String projRelPath = Paths.get(out).relativize(Paths.get(projectDir.replaceAll("\\\\", "/"))).toString();
                            projRelPath = projRelPath.replaceAll("\\\\", "/");


                            data = data.replaceAll("\\$\\{deployment_root_rel\\}", relPath);
                            data = data.replaceAll("\\$\\{proj_root_rel\\}", projRelPath);

                            return data;
                        }
                    });
                    createRunner(project, TnDecGroupFactory.TPL_RUN_CONFIG, projectDir);
                    SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

                } catch (IOException e) {
                    com.intellij.openapi.ui.Messages.showErrorDialog(project,"Error", e.getMessage());
                    e.printStackTrace();
                } finally {
                    ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
                }


                PopupUtil.showBalloonForActiveFrame("The project as been generated, please run npm install to load all needed dependencies", MessageType.INFO);
            });
        }

    }


    protected void createRunner(Project project, String template, String projectFolder) throws IOException {
        Path rootProjectPath = Paths.get(project.getBaseDir().getPath());
        Path runConfigPath = Paths.get(project.getBaseDir().getPath()+"/.idea/runConfigurations");
        Path angDir = Paths.get(projectFolder);

        Path rel =  rootProjectPath.relativize(angDir);

        Map<String, String> attrs = Maps.newHashMap();
        String angularType = isAngular1() ? "TinyDec" : "Angular NG";
        attrs.put("ANGULAR_TYPE", angularType);
        attrs.put("PKG_JSON_PATH", rel + "/package.json".replaceAll("\\\\","/") );

        FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(template);
        String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
        String fileName = ("Client Development Server Start ["+angularType+"].xml").replaceAll("\\s", "_");
        runConfigPath.toFile().mkdirs();
        SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

        IntellijUtils.create(project, LocalFileSystem.getInstance().findFileByPath(runConfigPath.toFile().getPath()), str, fileName);
    }

    @NotNull
    protected String getSubPath() {
        return "projectLayout/tnDec/";
    }

    @NotNull
    protected String getResourcePath() {
        return TN_PROJECT_LAYOUT;
    }

    protected boolean isAngular1() {
        return true;
    }
}