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
import net.werpu.tools.gui.CreateService;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.utils.IntellijUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.*;

/**
 * Create a Tiny Decs filter artifact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecFilter extends AnAction {


    public CreateTnDecFilter() {
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


        final net.werpu.tools.gui.CreateService mainForm = new net.werpu.tools.gui.CreateService();
        mainForm.getLblSelector().setText("Name *");
        mainForm.getLblTitle().setText(getTitle());
        //mainForm.getLblTemplate().setVisible(false);
        //mainForm.getLblControllerAs().setVisible(false);

        //mainForm.getTxtTemplate().setVisible(false);
        //mainForm.getTxtControllerAs().setVisible(false);
        mainForm.getLblExport().setText("Export Filter");
        mainForm.getCbExport().setSelected(ConfigSerializer.getInstance().getState().isFilterExport());

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.getMainPanel())
                .withDimensionKey("AnnFilter").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();


        dialogWrapper.setTitle(getDialogTitle());
        mainForm.getLblExport().setText(getExportLabel());
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            ControllerJson model = new ControllerJson((String) mainForm.getTxtName().getValue(), "", "");
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("The Filter/Pipe has been generated", "Info");
            ConfigSerializer.getInstance().getState().setDirectiveExport(mainForm.getCbExport().isSelected());
        }
    }

    @NotNull
    private ValidationInfo[] validateInput(CreateService mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty((String) mainForm.getTxtName().getValue(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                assertPattern((String) mainForm.getTxtName().getValue(), VALID_NAME, Messages.ERR_FILTER_PATTERN, mainForm.getTxtName())};
    }

    @NotNull
    protected String getExportLabel() {
        return "Export Filter";
    }

    @NotNull
    protected String getDialogTitle() {
        return "Create Filter";
    }

    @NotNull
    protected String getTitle() {
        return "Create an Annotated Filter";
    }

    void buildFile(Project project, ControllerJson model, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = model.getName();

            FileTemplate vslTemplate = getTemplate(project);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("NAME", className);

            generate(project, folder, className, vslTemplate, attrs);
        });
    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), ModuleElementScope.EXPORT, ModuleElementScope.DECLARATIONS).run();
    }

    protected FileTemplate getTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_FILTER);
    }
}
