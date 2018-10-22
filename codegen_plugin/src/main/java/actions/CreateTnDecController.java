package actions;

import actions_all.shared.GenerateFileAndAddRef;
import actions_all.shared.Messages;
import actions_all.shared.SimpleFileNameTransformer;
import actions_all.shared.VisibleAssertions;
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
import com.intellij.psi.PsiFileFactory;
import configuration.ConfigSerializer;
import dtos.ControllerJson;
import factories.TnDecGroupFactory;
import gui.CreateTnDecComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.dtos.ModuleElementScope;
import supportive.fs.common.IntellijFileContext;
import supportive.utils.IntellijUtils;
import supportive.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static actions_all.shared.FormAssertions.*;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecController extends AnAction  {


    public CreateTnDecController() {
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        VisibleAssertions.tnVisible(anActionEvent);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        //final Project project = IntellijUtils.getProject(event);




        final IntellijFileContext fileContext = new IntellijFileContext(event);

        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText("create.html",
                    HTMLLanguage.INSTANCE, "");

            Document document = workFile.getViewProvider().getDocument();


            ApplicationManager.getApplication().invokeLater(() -> {
                createDialog(fileContext.getProject(), fileContext.getVirtualFile(), document);
            });
        });
    }

    public void createDialog(Project project, VirtualFile folder, Document document) {
        final gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();
        mainForm.getLblSelector().setText("Name *");
        mainForm.getLblTitle().setText("Create an Annotated Controller");
        mainForm.getLblExport().setText("Export Controller");

        Editor editor = SwingUtils.createHtmlEditor(project, document);
        WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(""));



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
                        assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                        assertNotNullOrEmpty(mainForm.getControllerAs(), Messages.ERR_CTRL_AS_VALUE, mainForm.getTxtControllerAs()),
                        assertPattern(mainForm.getName(), VALID_NAME, Messages.ERR_SELECTOR_PATTERN, mainForm.getTxtName())
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

        dialogWrapper.setTitle("Create Controller");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));


        //mainForm.initDefault(dialogWrapper.getWindow());
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            String templateText = new String(editor.getDocument().getText().getBytes(), EncodingRegistry.getInstance().getDefaultCharset());
            ControllerJson model = new ControllerJson(mainForm.getName(), templateText, mainForm.getControllerAs());
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
            IntellijUtils.showInfoMessage("The Controller has been generated", "Info");
        }
    }


    void buildFile(Project project, ControllerJson model, VirtualFile folder) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String className = model.getName();

            FileTemplate vslTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_CONTROLLER);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());

            generate(project, folder, className, vslTemplate, attrs);
        });
    }

    protected void generate(Project project, VirtualFile folder, String className, FileTemplate vslTemplate, Map<String, Object> attrs) {
        new GenerateFileAndAddRef(project, folder, className, vslTemplate, attrs, new SimpleFileNameTransformer(), ModuleElementScope.EXPORT).run();
    }
}
