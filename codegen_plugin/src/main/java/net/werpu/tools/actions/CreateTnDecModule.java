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

package net.werpu.tools.actions;

import com.google.common.collect.Lists;
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
import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.Messages;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import net.werpu.tools.configuration.ConfigSerializer;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.gui.CreateTnDecComponent;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.*;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecModule extends AnAction {
    public static final String EXPORT = "___EXPORT___";

    public CreateTnDecModule() {
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

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.rootPanel)
                .withDimensionKey("AnnModule").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();

        dialogWrapper.setTitle("Create Module");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            boolean generateFolder = mainForm.getCbCreateDir().isSelected();
            boolean generateStructure = mainForm.getCbCreateStructure().isSelected();
            boolean exportModule = mainForm.getCbExport().isSelected();

            ControllerJson model = new ControllerJson(mainForm.getName(), mainForm.getTemplate(), mainForm.getControllerAs());

            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder, exportModule, generateFolder, generateStructure));
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("The Module has been generated", "Info");

            ConfigSerializer.getInstance().getState().setModuleExport(exportModule);
            ConfigSerializer.getInstance().getState().setModuleGenerateFolder(generateFolder);
            ConfigSerializer.getInstance().getState().setModuleGenerateStructure(generateStructure);
        }
    }

    @NotNull
    private ValidationInfo[] validateInput(CreateTnDecComponent mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                assertPattern(mainForm.getName(), VALID_NAME, Messages.ERR_MODULE_PATTERN, mainForm.getTxtName())};
    }

    void buildFile(Project project, ControllerJson model, final VirtualFile folder, boolean export, boolean createDir, boolean createStructure) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            VirtualFile finalFolder = folder;
            String name = model.getName();
            String className = StringUtils.toCamelCase(name);
            if (createDir) {

                try {
                    finalFolder = folder.createChildDirectory(project, getModuleName(className));

                } catch (IOException e) {
                    IntellijUtils.handleEx(project, e);
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
                    IntellijUtils.handleEx(project, e);
                    return;
                }

            }

            FileTemplate vslTemplate = getJ2eeTemplate(project);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("CLASS_NAME", className);
            attrs.put("NAME", name);

            if (export) {
                attrs.put(EXPORT, export);
            }

            generate(project, finalFolder, className, vslTemplate, attrs);
        });
    }

    protected String getModuleName(String className) {
        return className.toLowerCase().replaceAll("module$", "");
    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        List<ModuleElementScope> scope = Lists.newArrayList();

        if (attrs.containsKey(EXPORT)) {
            scope.add(ModuleElementScope.EXPORT);
        } else {
            scope.add(ModuleElementScope.IMPORT);
        }
        ModuleElementScope[] scopes = scope.stream().toArray(size -> new ModuleElementScope[size]);

        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), scopes).run();
    }

    protected FileTemplate getJ2eeTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_MODULE);
    }
}
