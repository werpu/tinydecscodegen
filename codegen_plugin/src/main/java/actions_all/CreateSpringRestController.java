package actions_all;

import actions_all.shared.Messages;
import actions_all.shared.GenerateFileAndAddRef;
import actions_all.shared.JavaFileNameTransformer;
import actions_all.shared.SimpleFileNameTransformer;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import configuration.ConfigSerializer;
import configuration.TinyDecsConfiguration;
import factories.TnDecGroupFactory;
import gui.CreateRestController;
import org.fest.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.dtos.ModuleElementScope;
import supportive.fs.common.IntellijFileContext;
import supportive.utils.IntellijUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static actions_all.shared.FormAssertions.assertNotNullOrEmpty;

public class CreateSpringRestController extends AnAction implements DumbAware {


    @Override
    public void update(AnActionEvent event) {

        final Project project = IntellijUtils.getProject(event);
        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        VirtualFile srcRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(folder);

        event.getPresentation().setEnabledAndVisible(srcRoot != null);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);
        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        IntellijFileContext ctx = new IntellijFileContext(project, folder);


        VirtualFile srcRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(folder);
        if (srcRoot != null) {//we are in a source root

            final gui.CreateRestController mainForm = new gui.CreateRestController();

            restore(mainForm);
            DialogWrapper dialogWrapper = new DialogWrapper(project, true, DialogWrapper.IdeModalityType.PROJECT) {

                @Nullable
                @Override
                protected JComponent createCenterPanel() {
                    return mainForm.getRootPanel();
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
                            assertNotNullOrEmpty(mainForm.getTxtServiceName().getText(), Messages.ERR_NAME_VALUE, mainForm.getTxtServiceName())
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

            dialogWrapper.show();
            if (dialogWrapper.isOK()) {
                String packageName = IntellijUtils.calculatePackageName(ctx, srcRoot);

                Map<String, Object> attrs = Maps.newHashMap();
                attrs.put("PACKAGE_NAME", packageName);

                String className = mainForm.getTxtServiceName().getText();
                attrs.put("CLASS_NAME", className);
                attrs.put("REQUEST_MAPPING", mainForm.getTxtRestPath().getText());

                buildFile(project, folder, className, attrs, event);

                ApplicationManager.getApplication().executeOnPooledThread(() -> {

                    WriteCommandAction.runWriteCommandAction(project, () -> {

                        IntellijUtils.fileNameTransformer = new SimpleFileNameTransformer();

                        PsiJavaFile jav = (PsiJavaFile) PsiManager.getInstance(project).findFile(ctx.getVirtualFile().findFileByRelativePath("./"+className+".java"));

                        if(mainForm.getCbCreate().isSelected()) {
                            try {
                                IntellijUtils.generateService(project, ctx.getModule(),jav, mainForm.getCbNg().isSelected());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });
                apply(mainForm);
            }
        }

        //dtermine the package

        //event.getProject().getWorkspaceFile().is

    }


    public void apply(CreateRestController mainForm) {
        TinyDecsConfiguration state = ConfigSerializer.getInstance().getState();
        state.setNgRest(mainForm.getCbNg().isSelected());

        state.setSyncTs(mainForm.getCbCreate().isSelected());
        state.setCalcRestService(mainForm.getCbCalcRest().isSelected());
    }

    public void restore(CreateRestController mainForm) {
        TinyDecsConfiguration state = ConfigSerializer.getInstance().getState();
        mainForm.getCbNg().setSelected(state.isNgRest());

        mainForm.getCbCreate().setSelected(state.isSyncTs());
        mainForm.getCbCalcRest().setSelected(state.isCalcRestService());
    }


    void buildFile(Project project, VirtualFile folder, String className, Map<String, Object> attrs, AnActionEvent event) {

        WriteCommandAction.runWriteCommandAction(project, () -> {

            FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_SPRING_REST);


            generate(project, folder, className, vslTemplate, attrs);
            supportive.utils.IntellijUtils.showInfoMessage("The Rest Controller has been generated", "Info");

        });


    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new JavaFileNameTransformer(), ModuleElementScope.DECLARATIONS).run();
    }

}
