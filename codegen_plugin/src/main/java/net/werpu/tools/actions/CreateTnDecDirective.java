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
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import com.intellij.psi.PsiFile;
import dtos.ComponentAttribute;
import dtos.DirectiveJson;
import net.werpu.tools.actions_all.shared.*;
import net.werpu.tools.configuration.ConfigSerializer;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.StringUtils;
import net.werpu.tools.supportive.utils.SwingUtils;
import org.jetbrains.annotations.NotNull;
import reflector.ComponentAttributesReflector;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.assertNotNullOrEmpty;
import static net.werpu.tools.actions_all.shared.FormAssertions.assertPattern;
import static net.werpu.tools.actions_all.shared.Messages.ERR_ELTYPE_SEL;
import static net.werpu.tools.supportive.utils.IntellijUtils.createRamFileFromText;
import static reflector.TransclusionReflector.getPossibleTransclusionSlots;
import static reflector.TransclusionReflector.hasTransclude;

public class CreateTnDecDirective extends AnAction {
    public static final String EXPORT = "___export___";

    public CreateTnDecDirective() {
        super();
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        final IntellijFileContext fileContext = new IntellijFileContext(event);

        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            PsiFile workFile = createRamFileFromText(fileContext.getProject(), "create.html",
                    "", HTMLLanguage.INSTANCE);

            Document document = workFile.getViewProvider().getDocument();
            Editor editor = SwingUtils.createHtmlEditor(fileContext.getProject(), document);
            editor.getDocument().setText("  ");

            ApplicationManager.getApplication().invokeLater(() -> {
                createDialog(fileContext.getProject(), fileContext.getVirtualFile(), document);
            });
        });

    }

    private void createDialog(Project project, VirtualFile folder, Document document) {
        final net.werpu.tools.gui.CreateTnDecDirective mainForm = new net.werpu.tools.gui.CreateTnDecDirective();

        //mainForm.getTxtTemplate().setVisible(false);
        Editor editor = SwingUtils.createHtmlEditor(project, document);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().setText("  ");
        });
        mainForm.getPnEditorHolder().getViewport().setView(editor.getComponent());

        mainForm.getCbExport().setSelected(ConfigSerializer.getInstance().getState().isDirectiveExport());

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.rootPanel)
                .withDimensionKey("AnnDirective").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();

        if (!isAngular1()) {
            mainForm.getElementCheckBox().setVisible(false);
            mainForm.getAttributeCheckBox().setVisible(false);
            mainForm.getClassCheckBox().setVisible(false);
            mainForm.getCommentCheckBox().setVisible(false);
        }
        dialogWrapper.setTitle("Create Directive");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        dialogWrapper.show();

        if (dialogWrapper.isOK()) {
            ApplicationManager.getApplication().invokeLater(() -> {

                String templateText = new String(editor.getDocument().getText().getBytes(), EncodingRegistry.getInstance().getDefaultCharset());
                DirectiveJson model = new DirectiveJson(mainForm.getName(),
                        templateText,
                        mainForm.getControllerAs(), buildTypes(mainForm),
                        hasTransclude(templateText), getPossibleTransclusionSlots(templateText));
                List<ComponentAttribute> attrs = ComponentAttributesReflector.reflect(templateText, mainForm.getControllerAs());

                boolean export = mainForm.getCbExport().isSelected();

                ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, attrs, folder, export));
                ConfigSerializer.getInstance().getState().setDirectiveExport(mainForm.getCbExport().isSelected());
            });
        }
    }

    @NotNull
    private ValidationInfo[] validateInput(net.werpu.tools.gui.CreateTnDecDirective mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_TAG_SELECTOR_MUST_HAVE_A_VALUE, mainForm.getTxtName()),
                assertNotNullOrEmpty(mainForm.getControllerAs(), Messages.ERR_CTRL_AS_VALUE, mainForm.getTxtControllerAs()),
                assertPattern(mainForm.getName(), FormAssertions.TAG_SELECTOR_PATTERN, Messages.ERR_TAG_SELECTOR_PATTERN, mainForm.getTxtName()),
                assertypes(mainForm)};
    }

    ValidationInfo assertypes(net.werpu.tools.gui.CreateTnDecDirective mainForm) {
        if (!(mainForm.getCommentCheckBox().isSelected() || mainForm.getClassCheckBox().isSelected() ||
                mainForm.getAttributeCheckBox().isSelected() || mainForm.getElementCheckBox().isSelected()
        )) {
            return new ValidationInfo(ERR_ELTYPE_SEL);
        }
        return null;
    }

    private String buildTypes(net.werpu.tools.gui.CreateTnDecDirective mainForm) {
        StringBuilder types = new StringBuilder();
        if (mainForm.getElementCheckBox().isSelected()) {
            types.append("E");
        }

        if (mainForm.getAttributeCheckBox().isSelected()) {
            types.append("A");
        }
        if (mainForm.getClassCheckBox().isSelected()) {
            types.append("C");
        }
        if (mainForm.getCommentCheckBox().isSelected()) {
            types.append("M");
        }

        return types.toString();
    }

    private void deleteWorkFile(Project project, VirtualFile vfile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                vfile.delete(project);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    void buildFile(Project project, DirectiveJson model, List<ComponentAttribute> cAttrs, VirtualFile folder, boolean export) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = StringUtils.toCamelCase(model.getSelector());

            FileTemplate vslTemplate = getFileTemplate(project);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("SELECTOR", model.getSelector());
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());
            attrs.put("TYPES", model.getTypes());
            attrs.put("COMPONENT_ATTRS", cAttrs);
            if (model.isTransclude() && model.getTransclusionSlots().isEmpty()) {
                attrs.put("TRANSCLUDE", true);
            }
            if (model.isTransclude() && !model.getTransclusionSlots().isEmpty()) {
                attrs.put("TRANSCLUDE_SLOTS", model.getTransclusionSlots());
            }
            if (export) {
                attrs.put(EXPORT, export);
            }
            generate(project, folder, className, vslTemplate, attrs);
            net.werpu.tools.supportive.utils.IntellijUtils.showInfoMessage("The Directive has been generated", "Info");
        });

    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        List<ModuleElementScope> scope = Lists.newArrayList();
        scope.add(ModuleElementScope.DECLARATIONS);
        if (attrs.containsKey(EXPORT)) {
            scope.add(ModuleElementScope.EXPORT);
        }

        ModuleElementScope[] scopes = scope.stream().toArray(size -> new ModuleElementScope[size]);
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), scopes).run();
    }

    protected FileTemplate getFileTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_DIRECTIVE);
    }

    protected boolean isAngular1() {
        return true;
    }
}
