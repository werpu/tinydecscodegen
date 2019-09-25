/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.actions_all;

import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.JavaFileNameTransformer;
import net.werpu.tools.actions_all.shared.Messages;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import net.werpu.tools.configuration.ConfigSerializer;
import net.werpu.tools.configuration.TinyDecsConfiguration;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.gui.CreateRestController;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.assertNotNullOrEmpty;

public class CreateSpringRestController extends AnAction {
    @Override
    public void update(AnActionEvent event) {

        final Project project = IntellijUtils.getProject(event);

        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        if (folder == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        VirtualFile srcRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(folder);

        //PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(srcRoot);
        //boolean isJavaSourceRoot = dir.getContext() instanceof PsiJavaDirectoryImpl;

        event.getPresentation().setEnabledAndVisible(srcRoot != null);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);
        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        IntellijFileContext ctx = new IntellijFileContext(project, folder);

        VirtualFile srcRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(folder);
        if (srcRoot != null) {//we are in a source root

            final net.werpu.tools.gui.CreateRestController mainForm = new net.werpu.tools.gui.CreateRestController();

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

                        PsiJavaFile javaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(ctx.getVirtualFile().findFileByRelativePath("./" + className + ".java"));

                        if (mainForm.getCbCreate().isSelected()) {
                            try {
                                IntellijUtils.generateService(project, ctx.getModule(), javaFile, mainForm.getCbNg().isSelected());
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
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("The Rest Controller has been generated", "Info");

        });

    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new JavaFileNameTransformer(), ModuleElementScope.DECLARATIONS).run();
    }

}
