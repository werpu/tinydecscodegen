package net.werpu.tools.actions;

import net.werpu.tools.actions_all.shared.GenerateFileAndAddRef;
import net.werpu.tools.actions_all.shared.Messages;
import net.werpu.tools.actions_all.shared.SimpleFileNameTransformer;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
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
import dtos.ControllerJson;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.gui.CreateTnDecComponent;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import org.jetbrains.annotations.NotNull;
import net.werpu.tools.supportive.dtos.ModuleElementScope;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.SwingUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.*;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecController extends AnAction {


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
        final net.werpu.tools.gui.CreateTnDecComponent mainForm = new net.werpu.tools.gui.CreateTnDecComponent();
        mainForm.getLblSelector().setText("Name *");
        mainForm.getLblTitle().setText("Create an Annotated Controller");
        mainForm.getLblExport().setText("Export Controller");

        Editor editor = SwingUtils.createHtmlEditor(project, document);
        WriteCommandAction.runWriteCommandAction(project, () -> editor.getDocument().setText(""));


        mainForm.getPnEditorHolder().getViewport().setView(editor.getComponent());

        DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(project, mainForm.rootPanel)
                .withDimensionKey("AnnController").withValidator(() -> Arrays.asList(
                        validateInput(mainForm)
                ).stream().filter(s -> s != null).collect(Collectors.toList())).create();


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

    @NotNull
    private ValidationInfo[] validateInput(CreateTnDecComponent mainForm) {
        return new ValidationInfo[]{assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                assertNotNullOrEmpty(mainForm.getControllerAs(), Messages.ERR_CTRL_AS_VALUE, mainForm.getTxtControllerAs()),
                assertPattern(mainForm.getName(), VALID_NAME, Messages.ERR_CONTROLLER_PATTERN, mainForm.getTxtName())};
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