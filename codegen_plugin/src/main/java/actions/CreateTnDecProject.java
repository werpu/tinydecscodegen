package actions;

import actions_all.shared.VisibleAssertions;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import gui.support.UIActionSequence;
import gui.support.InputDialogWrapperBuilder;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static actions_all.shared.FormAssertions.assertNotNullOrEmpty;

public class CreateTnDecProject extends AnAction {

    private static final String TN_PROJECT_LAYOUT = "/resources/projectLayout/tnDec";
    private static final Dimension PREFERRED_SIZE = new Dimension(400, 300);
    public static final String RE_DEPLOYMENT_ROOT = "\\$\\{deployment_root_rel}";
    public static final String RE_PROJ_ROOT = "\\$\\{proj_root_rel}";
    public static final String RE_BACKSLASH = "\\\\";
    public static final String SLASH = "/";

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnNoProject(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Project project = IntellijUtils.getProject(anActionEvent);

        VirtualFile file = anActionEvent.getDataContext().getData(DataKeys.VIRTUAL_FILE);
        //VirtualFile folder = file.getParent();
        if(file == null) {
            IntellijUtils.showInfoMessage("You need to select a parent directory for your project", "Info");
            return;
        }

        String path = file.isDirectory() ? file.getPath() : file.getParent().getPath();

        String outputPath = path + "/../target";
        Path output = Paths.get(outputPath);
        output = output.normalize();

        createDialog(project, path, output.toFile().getPath());


    }

    private void createDialog(Project project, String projectFolder, String targetFolder) {
        final gui.CreateTnProject mainForm = new gui.CreateTnProject();

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.rootPanel)
                .withDimensionKey("AnnProject").withValidator(validationCallback(mainForm))
                .withTitle("Create Project")
                .withPreferredSize(PREFERRED_SIZE)
                .withOkHandler(() -> okPressed(project, mainForm))
                .create();


        mainForm.setProject(project);
        mainForm.getLblTitle().setText(getTitle());

        mainForm.projectDir.setText(projectFolder);
        mainForm.targetDir.setText(targetFolder);


        dialogWrapper.show();
    }

    @NotNull
    private Supplier<List<ValidationInfo>> validationCallback(CreateTnProject mainForm) {
        return () -> Arrays.stream(
                validateInput(mainForm)
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean okPressed(Project project, CreateTnProject mainForm) {
        String projectName = mainForm.getTxtProjectName().getText();
        String projectDir = mainForm.projectDir.getText();
        boolean createDir = mainForm.getCbCreateDir().isSelected();

        projectDir = projectDir.replaceAll("\\\\", SLASH);
        if (createDir) {
            if (!projectDir.endsWith(SLASH)) {
                projectDir = projectDir + SLASH;
            }
            projectDir = projectDir + projectName;
        }


        try {
            Path pProjectDir = Paths.get(projectDir);
            boolean targetPresent = Files.exists(pProjectDir) && Files.list(pProjectDir).findAny().isPresent();
            if (targetPresent) {
                int result = Messages.showYesNoDialog("The project target directory already contains files some of the files might be overwritten, do you like to proceed?", "Overwrite Warning", null);
                if (result == Messages.NO) {
                    IntellijUtils.showInfoMessage("Project generation was cancelled", "Info");
                    return false;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String fProjectDir = projectDir;


        UIActionSequence sequence = new UIActionSequence(project, "Running npm install")
                .withSequence(progressIndicator -> {
                    progressIndicator.setIndeterminate(true);
                    progressIndicator.setText("Creating Project");
                    writeProject(fProjectDir, mainForm);
                })
                .and(progressIndicator -> {
                    progressIndicator.setText("Creating the run configurations");
                    createRunConfig(project, projectName, fProjectDir);
                })
                .and(progressIndicator -> refresh())
                .withFinishedOk(progressIndicator -> IntellijUtils.npmInstall(project, fProjectDir, "The project has been generated successfully", "Success"));

        sequence.run();


        return true;
    }

    private void writeProject(String fProjectDir, CreateTnProject mainForm) {
        try {
            FileDocumentManager.getInstance().saveAllDocuments();
            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
            IntellijResourceDir resources = new IntellijResourceDir(getResourcePath(), getSubPath());


            resources.copyTo(new File(fProjectDir), (out, data) -> {


                String relPath = Paths.get(out).relativize(Paths.get(mainForm.targetDir.getText().replaceAll(RE_BACKSLASH, SLASH))).toString();
                relPath = relPath.replaceAll(RE_BACKSLASH, SLASH);
                String projRelPath = Paths.get(out).relativize(Paths.get(fProjectDir.replaceAll(RE_BACKSLASH, SLASH))).toString();


                data = data.replaceAll(RE_DEPLOYMENT_ROOT, relPath);
                data = data.replaceAll(RE_PROJ_ROOT, projRelPath);

                return data;
            });

            SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

            IntellijUtils.showInfoMessage("Project setup done, now starting npm to install all needed dependencies", "Info");

        } finally {
            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
            //progressIndicator.stop();
        }
    }

    private void createRunConfig(Project project, String projectName, String fProjectDir) {
        try {
            FileDocumentManager.getInstance().saveAllDocuments();
            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
            createRunner(project, TnDecGroupFactory.TPL_RUN_CONFIG, projectName, fProjectDir);
        } catch (IOException e) {
            IntellijUtils.showErrorDialog(project, "Error", e.getMessage());
            e.printStackTrace();
        } finally {
            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
        }
    }

    private void refresh() {
        try {
            FileDocumentManager.getInstance().saveAllDocuments();
            ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
        } finally {
            ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
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
     * @param project the intellij project
     * @param template template name
     * @param projectFolder the project target folder
     *
     * @throws IOException some internal ops might bomb with an io ex
     */
    protected void createRunner(Project project, String template, String projectName, String projectFolder) throws IOException {
        Path rootProjectPath = Paths.get(project.getBaseDir().getPath());
        Path runConfigPath = Paths.get(project.getBaseDir().getPath() + "/.idea/runConfigurations");
        Path angDir = Paths.get(projectFolder);

        Path rel = rootProjectPath.relativize(angDir);

        Map<String, String> attrs = Maps.newHashMap();
        String angularType = isAngular1() ? "TinyDec" : "Angular NG";
        attrs.put("ANGULAR_TYPE", angularType);
        attrs.put("PKG_JSON_PATH", (rel + "/package.json").replaceAll("\\\\", SLASH));
        attrs.put("PROJECT_NAME", projectName);

        FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(template);
        String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
        String fileName = ("Client Start - " + projectName + " [" + angularType + "].xml").replaceAll("\\s", "_");

        if(runConfigPath.toFile().mkdirs()) {
            //noop maybe a log later
        }
        SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);

        VirtualFile runConfig = LocalFileSystem.getInstance().findFileByPath(runConfigPath.toFile().getPath() + SLASH + fileName);
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