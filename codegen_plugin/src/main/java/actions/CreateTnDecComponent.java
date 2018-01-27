package actions;

import actions.shared.GenerateFileAndAddRef;
import actions.shared.SimpleFileNameTransformer;
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
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import dtos.ComponentAttribute;
import dtos.ComponentJson;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reflector.ComponentAttributesReflector;
import reflector.TransclusionReflector;
import utils.IntellijFileContext;
import utils.IntellijUtils;
import utils.ModuleElementScope;
import utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static actions.FormAssertions.assertNotNullOrEmpty;
import static actions.FormAssertions.assertPattern;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecComponent extends AnAction implements DumbAware {


    public CreateTnDecComponent() {
        super();
    }


    @Override
    public void actionPerformed(AnActionEvent event) {

        final IntellijFileContext fileContext = new IntellijFileContext(event);

        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText("create.html",
                    HTMLLanguage.INSTANCE, "");

            Document document = workFile.getViewProvider().getDocument();
            Editor editor = SwingUtils.createHtmlEditor(fileContext.getProject(), document);
            editor.getDocument().setText("  ");

            ApplicationManager.getApplication().invokeLater(() -> {
                createDialog(fileContext.getProject(), fileContext.getVirtualFile(), document);
            });
        });


    }

    private void createDialog(Project project, VirtualFile folder, Document document) {
        final gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();

        if(!isAngular1()) {
            mainForm.getLblControllerAs().setVisible(false);
            mainForm.getTxtControllerAs().setVisible(false);
        }


        mainForm.getTxtTemplate().setVisible(false);
        Editor editor = SwingUtils.createHtmlEditor(project, document);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().setText("  ");
        });
        mainForm.getPnEditorHolder().getViewport().setView(editor.getComponent());

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


            @Nullable
            @NotNull
            protected List<ValidationInfo> doValidateAll() {
                return Arrays.asList(
                        assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_TAG_SELECTOR_MUST_HAVE_A_VALUE, mainForm.getTxtName()),
                        assertNotNullOrEmpty(mainForm.getControllerAs(), Messages.ERR_CTRL_AS_VALUE, mainForm.getTxtControllerAs()),
                        assertPattern(mainForm.getName(), FormAssertions.TAG_SELECTOR_PATTERN, Messages.ERR_TAG_SELECTOR_PATTERN, mainForm.getTxtName())
                ).stream().filter(s -> s != null).collect(Collectors.toList());
            }


            @Override
            public void init() {
                super.init();
            }

            public void show() {
                this.init();
                this.setModal(false);
                this.pack();
                super.show();
            }

            @Override
            protected void doOKAction() {
                ApplicationManager.getApplication().invokeLater(() -> {

                    String templateText = new String(editor.getDocument().getText().getBytes(), EncodingRegistry.getInstance().getDefaultCharset()).toString();
                    ComponentJson model = new ComponentJson(mainForm.getName(), templateText, mainForm.getControllerAs(),
                            hasTransclude(templateText), getPossibleTransclusionSlots(templateText));
                    List<ComponentAttribute> attrs = getCompAttrs(editor, mainForm);
                    ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, attrs, folder));

                });
                super.doOKAction();
            }
        };

        dialogWrapper.setTitle("Create Component");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        dialogWrapper.show();
    }

    protected List<ComponentAttribute> getCompAttrs(Editor editor, gui.CreateTnDecComponent mainForm) {
        return ComponentAttributesReflector.reflect(editor.getDocument().getText(), mainForm.getControllerAs());
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

    void buildFile(Project project, ComponentJson model, List<ComponentAttribute> cAttrs, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = IntellijUtils.toCamelCase(model.getSelector());

            FileTemplate vslTemplate = getTemplate(project);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("SELECTOR", model.getSelector());
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());

            attrs.put("COMPONENT_ATTRS", cAttrs);

            if(model.isTransclude() && model.getTransclusionSlots().isEmpty()) {
                attrs.put("TRANSCLUDE", true);
            }
            if(model.isTransclude() && !model.getTransclusionSlots().isEmpty()) {
                attrs.put("TRANSCLUDE_SLOTS", model.getTransclusionSlots());
            }
            generate(project, folder, className, vslTemplate, attrs);

        });


    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), ModuleElementScope.DECLARATIONS, ModuleElementScope.EXPORT).run();
    }


    protected List<String> getPossibleTransclusionSlots(String templateText) {
        return TransclusionReflector.getPossibleTransclusionSlots(templateText);
    }

    protected boolean hasTransclude(String templateText) {
        return TransclusionReflector.hasTransclude(templateText);
    }

    protected FileTemplate getTemplate(Project project) {
        return FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT);
    }

    protected boolean isAngular1() {
        return true;
    }

}
