package actions;

import actions_all.shared.VisibleAssertions;
import com.google.common.collect.Maps;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import factories.TnDecGroupFactory;
import gui.CreateTnProject;
import gui.support.DialogWrapperCreator;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.IntellijResourceDir;
import supportive.fs.common.TextTransformer;
import supportive.utils.IntellijUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static actions_all.shared.FormAssertions.assertNotNullOrEmpty;

public class CreateTnDecProject extends AnAction {

    public static final String TN_PROJECT_LAYOUT = "/resources/projectLayout/tnDec";

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnNoProject(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Project project = IntellijUtils.getProject(anActionEvent);

        VirtualFile file = (VirtualFile) anActionEvent.getDataContext().getData(DataKeys.VIRTUAL_FILE);
        //VirtualFile folder = file.getParent();


        String path = file.isDirectory() ? file.getPath() : file.getParent().getPath();

        String outputPath = path + "/../target";
        Path output = Paths.get(outputPath);
        output = output.normalize();

        createDialog(project, path, output.toFile().getPath());


    }

    private void createDialog(Project project, String projectFolder, String targetFolder) {
        final gui.CreateTnProject mainForm = new gui.CreateTnProject();

        DialogWrapper dialogWrapper = new DialogWrapperCreator(project, mainForm.rootPanel)
                .withDimensionKey("AnnProject").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();


        mainForm.setProject(project);
        mainForm.getLblTitle().setText(getTitle());
        mainForm.projectDir.setText(projectFolder);
        mainForm.targetDir.setText(targetFolder);

        dialogWrapper.setTitle("Create Project");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        dialogWrapper.show();

        if (dialogWrapper.isOK()) {
            String projectName = mainForm.getTxtProjectName().getText();
            String projectDir = mainForm.projectDir.getText();
            boolean createDir = mainForm.getCbCreateDir().isSelected();

            projectDir = projectDir.replaceAll("\\\\", "/");
            if(createDir) {
                if(!projectDir.endsWith("/")) {
                    projectDir = projectDir + "/";
                }
                projectDir = projectDir + projectName;
            }


            try {
                Path pProjectDir = Paths.get(projectDir);
                boolean targetPresent = Files.exists(pProjectDir) && Files.list(pProjectDir).findAny().isPresent();
                if (targetPresent) {
                    int result = Messages.showYesNoDialog("The project target directory already contains files some of the files might be overwritten, do you like to proceed?", "Overwrite Warning", null);
                    if (result == Messages.NO) {
                        supportive.utils.IntellijUtils.showInfoMessage("Project generation was cancelled", "Info");
                        return;
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final String fProjectDir = projectDir;


            /*ProgressManager progressManager = ProgressManager.getInstance();
            */

            final Task.Backgroundable myTask = new Task.Backgroundable(project, "calling npm install") {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {

                    progressIndicator.setIndeterminate(true);
                    progressIndicator.setText("Creating Project");

                    WriteCommandAction.runWriteCommandAction(project, () -> {

                        try {
                            FileDocumentManager.getInstance().saveAllDocuments();
                            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
                            IntellijResourceDir resources = new IntellijResourceDir(getResourcePath(), getSubPath());


                            resources.copyTo(new File(fProjectDir), new TextTransformer() {
                                @Override
                                public String transform(String out, String data) {


                                    String relPath = Paths.get(out).relativize(Paths.get(mainForm.targetDir.getText().replaceAll("\\\\", "/"))).toString();
                                    relPath = relPath.replaceAll("\\\\", "/");
                                    String projRelPath = Paths.get(out).relativize(Paths.get(fProjectDir.replaceAll("\\\\", "/"))).toString();


                                    data = data.replaceAll("\\$\\{deployment_root_rel\\}", relPath);
                                    data = data.replaceAll("\\$\\{proj_root_rel\\}", projRelPath);

                                    return data;
                                }
                            });

                            SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
                            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

                            supportive.utils.IntellijUtils.showInfoMessage("Project setup done, now starting npm to install all needed dependencies", "Info");

                        } finally {
                            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
                            //progressIndicator.stop();
                        }


                    });



                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            FileDocumentManager.getInstance().saveAllDocuments();
                            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
                            createRunner(project, TnDecGroupFactory.TPL_RUN_CONFIG, projectName, fProjectDir);
                        } catch (IOException e) {
                            supportive.utils.IntellijUtils.showErrorDialog(project, "Error", e.getMessage());
                            e.printStackTrace();
                        } finally {
                            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
                        }
                    });
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            FileDocumentManager.getInstance().saveAllDocuments();
                            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
                            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
                        } finally {
                            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
                        }
                    });
                    ApplicationManager.getApplication().invokeLater(() -> {

                        IntellijUtils.npmInstall(project, fProjectDir, "The project has been generated successfully", "Success");

                    });

                }
            };
            BackgroundableProcessIndicator myProcessIndicator = new BackgroundableProcessIndicator(myTask);
            myProcessIndicator.setText("Running npm install");


            ProgressManager.getInstance().runProcessWithProgressAsynchronously(myTask, myProcessIndicator);



        }

    }

    @NotNull
    private ValidationInfo[] validateInput(CreateTnProject mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty(mainForm.getTxtProjectName().getText(), actions_all.shared.Messages.ERR_PROJECT_NO_NAME, mainForm.getTxtProjectName()),
                assertNotNullOrEmpty(mainForm.projectDir.getText(), actions_all.shared.Messages.ERR_PROJECT_DIR_CHOSEN, mainForm.getProjectDir()),
                assertNotNullOrEmpty(mainForm.targetDir.getText(), actions_all.shared.Messages.ERR_TARGET_DIR_CHOSEN, mainForm.getTargetDir())};
    }

    @NotNull
    protected String getTitle() {
        return "Create Tiny Decorations Project";
    }


    /**
     * create a new run configuratin
     *
     * @param project
     * @param template
     * @param projectFolder
     * @throws IOException
     */
    protected void createRunner(Project project, String template, String projectName, String projectFolder) throws IOException {
        Path rootProjectPath = Paths.get(project.getBaseDir().getPath());
        Path runConfigPath = Paths.get(project.getBaseDir().getPath() + "/.idea/runConfigurations");
        Path angDir = Paths.get(projectFolder);

        Path rel = rootProjectPath.relativize(angDir);

        Map<String, String> attrs = Maps.newHashMap();
        String angularType = isAngular1() ? "TinyDec" : "Angular NG";
        attrs.put("ANGULAR_TYPE", angularType);
        attrs.put("PKG_JSON_PATH", (rel + "/package.json").replaceAll("\\\\", "/"));
        attrs.put("PROJECT_NAME", projectName);

        FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(template);
        String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
        String fileName = ("Client Start - "+projectName+" [" + angularType + "].xml").replaceAll("\\s", "_");
        runConfigPath.toFile().mkdirs();
        SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

        VirtualFile runConfig = LocalFileSystem.getInstance().findFileByPath(runConfigPath.toFile().getPath() + "/" + fileName);
        if (runConfig != null && runConfig.exists()) {
            supportive.utils.IntellijUtils.showInfoMessage("Run Config already exists, skipping run config generation", "Info");
        } else {
            IntellijUtils.create(project, LocalFileSystem.getInstance().findFileByPath(runConfigPath.toFile().getPath()), str, fileName);
        }

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