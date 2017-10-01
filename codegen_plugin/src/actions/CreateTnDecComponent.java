package actions;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.FileTemplateBase;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import dtos.ComponentJson;
import factories.TnDecGroupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.IntellijRefactor;
import utils.IntellijUtils;
import utils.RefactorUnit;
import utils.SwingUtils;

import javax.swing.*;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create a Tiny Decs artefact.
 * The idea is that every created artifact should auto register if possible
 */
public class CreateTnDecComponent extends AnAction implements DumbAware {


    public CreateTnDecComponent() {
        //super("TDecs Angular ComponentJson", "Creates a Tiny Decorations Angular ComponentJson", null);
        super();
    }

   /* @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("Ts").addKind("Java File", JavaFileType.INSTANCE.getIcon(), "Java File")
                .addKind("Java File2", JavaFileType.INSTANCE.getIcon(), "Java File");
        ;

    }*/

    //@Override
    //protected String getActionName(PsiDirectory psiDirectory, String s, String s1) {
    //    return null;
    //}

    //https://intellij-support.jetbrains.com/hc/en-us/community/posts/206755515-CreateFileFromTemplateDialog-with-additional-input-fields
    //@Override

    /**
     * decided whether the menu item should be shown
     */
    /*public void update(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();

        boolean enabled = true;//whatever criteria you have for when it's visible/enabled
        Presentation presentation = event.getPresentation();
        presentation.setVisible(enabled);
        presentation.setEnabled(enabled);
    }

    @Override
    public void beforeActionPerformedUpdate(@NotNull AnActionEvent e) {
        super.beforeActionPerformedUpdate(e);
    }
*/
    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = IntellijUtils.getProject(event);


        VirtualFile file = (VirtualFile) event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
        VirtualFile folder = file;


        final gui.CreateTnDecComponent mainForm = new gui.CreateTnDecComponent();

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
            @Override
            protected ValidationInfo doValidate() {

                if (Strings.isNullOrEmpty(mainForm.getName()) || Strings.isNullOrEmpty(mainForm.getControllerAs())) {
                    ValidationInfo info = new ValidationInfo("Tag selector and Controller As must have values");
                    return info;
                }
                return null;
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
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            ComponentJson model = new ComponentJson(mainForm.getName(), mainForm.getTemplate(), mainForm.getControllerAs());
            ApplicationManager.getApplication().invokeLater(() -> buildFile(project, model, folder));
        }
    }

    void buildFile(Project project, ComponentJson model, VirtualFile folder) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            String className = toCamelCase(model.getSelector());

            FileTemplate vslTemplate = (FileTemplateBase)
                    FileTemplateManager.getInstance(project).getJ2eeTemplate(TnDecGroupFactory.TPL_ANNOTATED_COMPONENT);

            Map<String, Object> attrs = Maps.newHashMap();
            attrs.put("SELECTOR", model.getSelector());
            attrs.put("NAME", className);
            attrs.put("TEMPLATE", model.getTemplate());
            attrs.put("CONTROLLER_AS", model.getControllerAs());

            try {
                String str = FileTemplateUtil.mergeTemplate(attrs, vslTemplate.getText(), false);
                String fileNmae = className + ".ts";

                List<PsiFile> annotatedModules = IntellijUtils.findFirstAnnotatedClass(project, folder, IntellijRefactor.NG_MODULE);
                for (PsiFile module : annotatedModules) {
                    List<PsiElement> elements = IntellijRefactor.findAnnotatedElements(project, module, IntellijRefactor.NG_MODULE);
                    List<RefactorUnit> refactoringsToProcess = elements.stream().map(element -> {
                        return refactorAddExport(className, module, element);
                    }).collect(Collectors.toList());
                    String refactoredText = IntellijRefactor.refactor(refactoringsToProcess);
                    VirtualFile vModule = module.getVirtualFile();

                    vModule.setBinaryContent(refactoredText.getBytes());

                    //PsiDocumentManager.getInstance(project).commitDocument(FileDocumentManager.getInstance().getCachedDocument(vModule));
                    //PsiElement reformatted = CodeStyleManager.getInstance(project).reformat(PsiManager.getInstance(project).findFile(vModule));
                    //vModule.setBinaryContent(reformatted.getText().getBytes());
                }
                IntellijUtils.createAndOpen(project, folder, str, fileNmae);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @NotNull
    private RefactorUnit refactorAddExport(String className, PsiFile module, PsiElement element) {
        String elementText = element.getText();
        String rawData = elementText.substring(elementText.indexOf("(") + 1, elementText.lastIndexOf(")"));
        String refactoredData = IntellijRefactor.NG_MODULE + "(" + IntellijRefactor.appendExport(rawData, className) + ")";

        return new RefactorUnit(module, element, refactoredData);
    }


    //https://stackoverflow.com/questions/1086123/string-conversion-to-title-case
    public static String toCamelCase(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replaceAll("-", "");
    }

}
