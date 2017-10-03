package actions;

import actions.shared.GenerateFileAndAddRef;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import dtos.ComponentAttribute;
import dtos.ComponentJson;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reflector.ComponentAttributesReflector;
import utils.IntellijUtils;
import utils.ModuleElementScope;

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
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);
        VirtualFile folder = IntellijUtils.getFolderOrFile(event);
        final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(folder);




        final gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();


        final VirtualFile vfile = createWorkFile(project, module);
        Document document = FileDocumentManager.getInstance().getDocument(vfile);

        mainForm.getTxtTemplate().setVisible(false);
        Editor editor = createHtmlEditor(project,document);
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
                this.setModal(true);
                this.pack();
                super.show();
            }
        };


        dialogWrapper.setTitle("Create Component");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        ApplicationManager.getApplication().invokeLater(() -> {
            dialogWrapper.show();
            if (dialogWrapper.isOK()) {
                ComponentJson model = new ComponentJson(mainForm.getName(), editor.getDocument().getText(), mainForm.getControllerAs());
                List<ComponentAttribute> attrs = ComponentAttributesReflector.reflect(editor.getDocument().getText(), mainForm.getControllerAs());
                ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, attrs, folder));
            }
            deleteWorkFile(project, vfile);
        });

    }

    @Nullable
    private VirtualFile createWorkFile(Project project, Module module) {
        VirtualFile vfile1 = null;
        try {
            vfile1 = module.getModuleFile().getParent().createChildData(project, "__create__cc___.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vfile1;
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

    @NotNull
    public static Editor createHtmlEditor(Project project,  Document document) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(document, project, FileTypeManager.getInstance().getFileTypeByExtension(".html"), false);

        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setLineMarkerAreaShown(true);
        editorSettings.setLineNumbersShown(true);
        editorSettings.setFoldingOutlineShown(true);
        editorSettings.setAnimatedScrolling(true);
        editorSettings.setWheelFontChangeEnabled(true);
        editorSettings.setVariableInplaceRenameEnabled(true);
        editorSettings.setDndEnabled(true);
        editorSettings.setAutoCodeFoldingEnabled(true);
        editorSettings.setSmartHome(true);

        return editor;
    }

    void buildFile(Project project, ComponentJson model,List<ComponentAttribute> cAttrs, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = IntellijUtils.toCamelCase(model.getSelector());

            FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("SELECTOR", model.getSelector());
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());

            attrs.put("COMPONENT_ATTRS", cAttrs);

            new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, ModuleElementScope.EXPORT).run();

        });


    }


}
