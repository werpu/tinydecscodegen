package net.werpu.tools.actions_all;

import net.werpu.tools.actions_all.shared.JavaFileContext;
import net.werpu.tools.actions_all.shared.Messages;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import net.werpu.tools.configuration.ConfigSerializer;
import net.werpu.tools.configuration.TinyDecsConfiguration;
import net.werpu.tools.factories.TnDecGroupFactory;
import net.werpu.tools.gui.CreateRequestMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions;
import net.werpu.tools.supportive.utils.IntellijUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.FormAssertions.assertNotNullOrEmpty;
import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotJavaRest;
import static net.werpu.tools.actions_all.shared.VisibleAssertions.assertNotSpringRest;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.PSI_CLASS;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.PSI_METHOD;

enum SupportedRestMethod {
    PUT, GET, DELETE, POST
}


public class CreateRestEndpoint extends AnAction {

    //visibility
    @Override
    public void update(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (assertNotJavaRest(ctx) && assertNotSpringRest(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }

        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        if (event.getData(PlatformDataKeys.EDITOR) == null) {
            net.werpu.tools.supportive.utils.IntellijUtils.showErrorDialog(event.getProject(), "Error", "No editor found, please focus on an open source file");
            return;
        }
        JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;


        final net.werpu.tools.gui.CreateRequestMapping mainForm = new net.werpu.tools.gui.CreateRequestMapping();

        restore(mainForm);

        DialogWrapper dialogWrapper = new DialogWrapper(event.getProject(), true, DialogWrapper.IdeModalityType.PROJECT) {

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
                        assertNotNullOrEmpty(mainForm.getTxtMethodName().getText(), Messages.ERR_NAME_VALUE, mainForm.getTxtMethodName())
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
            //ok insert the text and then proceed with the generation
            //map the parameters
            SupportedRestMethod method = mapRestMethod(mainForm);
            String javaMethodName = mainForm.getTxtMethodName().getText();
            String restPath = mainForm.getTxtRestPath().getText();

            String returnType = mainForm.getTxtReturnType().getText();
            returnType = (Strings.isNullOrEmpty(returnType)) ? "void" : returnType;
            boolean isList = mainForm.getCbList().isSelected();
            if (isList) {
                returnType = "List<" + returnType + ">";
            }
            boolean createMapping = mainForm.getCbTypeScript().isSelected();


            //Lets generate the needed text from the template
            //with the given meta data params


            Map<String, String> params = Maps.newHashMap();


            params.put("REQUEST_METHOD", method.name());
            params.put("METHOD_NAME", javaMethodName);
            params.put("RETURN_TYPE", returnType);
            params.put("REST_PATH", restPath);


            FileTemplate vslTemplate = FileTemplateManager.getInstance(event.getProject()).getJ2eeTemplate(TnDecGroupFactory.TPL_SPRING_REST_METHOD);
            try {
                String insertText = FileTemplateUtil.mergeTemplate(params, vslTemplate.getText(), false);

                //insert the text into the active java editor
                insertIntoEditor(event, insertText);
                IntellijFileContext ctx = new IntellijFileContext(event);
                reformat(javaMethodName, ctx);
                if (mainForm.getCbTypeScript().isSelected()) {
                    try {

                        IntellijUtils.generateService(event.getProject(), ctx.getModule(), (PsiJavaFile) ctx.getPsiFile(), mainForm.getRbAngNg().isSelected());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            apply(mainForm);

        }

    }

    public void apply(CreateRequestMapping mainForm) {
        TinyDecsConfiguration state = ConfigSerializer.getInstance().getState();
        state.setNgRest(mainForm.getRbAngNg().isSelected());
        SupportedRestMethod defaultRestMethod = mainForm.getRbGet().isSelected() ? SupportedRestMethod.GET :
                mainForm.getRbPost().isSelected() ? SupportedRestMethod.POST :
                        mainForm.getRbPut().isSelected() ? SupportedRestMethod.PUT :
                                SupportedRestMethod.DELETE;
        state.setRestType(defaultRestMethod.name());
        state.setSyncTs(mainForm.getCbTypeScript().isSelected());
        state.setCalcRest(mainForm.getCbCalcRest().isSelected());
    }

    public void restore(CreateRequestMapping mainForm) {
        TinyDecsConfiguration state = ConfigSerializer.getInstance().getState();
        mainForm.getRbAngNg().setSelected(state.isNgRest());
        SupportedRestMethod defaultRestMethod = SupportedRestMethod.valueOf(state.getRestType());

        switch (defaultRestMethod) {
            case GET:
                mainForm.getRbGet().setSelected(true);
                break;
            case POST:
                mainForm.getRbPost().setSelected(true);
                break;
            case PUT:
                mainForm.getRbPut().setSelected(true);
                break;
            case DELETE:
                mainForm.getRbDelete().setSelected(true);
                break;
        }
        mainForm.getCbTypeScript().setSelected(state.isSyncTs());
        mainForm.getCbCalcRest().setSelected(state.isCalcRest());
    }

    public void reformat(String javaMethodName, IntellijFileContext ctx) {

        WriteCommandAction.runWriteCommandAction(ctx.getProject(), () -> {
            ctx.reformat();
        });
    }

    public void insertIntoEditor(AnActionEvent event, String insertText) {
        Editor editor = IntellijUtils.getEditor(event);
        final int cursorPos = editor.getCaretModel().getOffset();
        IntellijFileContext editorFile = new IntellijFileContext(event);
        Optional<PsiElement> after = editorFile.$q(PSI_METHOD)
                .map(el -> el.getElement())
                .filter(el -> el.getTextOffset() >= cursorPos).findFirst();
        List<PsiElement> before = editorFile.$q(PSI_METHOD)
                .map(el -> el.getElement())
                .filter(el -> el.getTextOffset() <= cursorPos).collect(Collectors.toList());

        PsiElement beforeElement = (before.size() > 0) ? before.get(before.size() - 1) : null;


        WriteCommandAction.runWriteCommandAction(editorFile.getProject(), () -> {

            if (after.isPresent() && beforeElement != null &&
                    beforeElement.getTextRange().getEndOffset() <= cursorPos &&
                    after.get().getTextRange().getStartOffset() >= cursorPos
            ) {

                editor.getDocument().insertString(cursorPos, insertText);
            } else if (after.isPresent()) {
                editor.getDocument().insertString(after.get().getTextRange().getStartOffset(), insertText);
            } else if (beforeElement != null) {

                editor.getDocument().insertString(beforeElement.getTextRange().getEndOffset() + 1, insertText);
            } else {

                List<PsiElement> classes = editorFile.$q(PSI_CLASS).map(el -> el.getElement()).collect(Collectors.toList());
                PsiElement insertClass = findNearest(cursorPos, classes);


                if (insertClass != null) {
                    PsiElement element = insertClass;
                    int finalOffset = element.getStartOffsetInParent() + element.getTextLength();
                    editor.getDocument().insertString(finalOffset - 1, insertText);
                } else {
                    editor.getDocument().insertString(cursorPos, insertText);
                }

            }
            try {
                editorFile.commit();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * find the nearest class to the current cursorPos
     * we use a distance calc to check where the nearest class is located
     *
     * @param cursorPos
     * @param classes
     * @return
     */
    @Nullable
    public PsiElement findNearest(int cursorPos, List<PsiElement> classes) {
        int distance = 100000;
        PsiElement insertClass = null;
        for (PsiElement clazz : classes) {
            int startOffset = clazz.getStartOffsetInParent();
            int endOffset = clazz.getStartOffsetInParent() + clazz.getTextLength();
            int newDistance = Math.min(Math.abs(cursorPos - startOffset), Math.abs(cursorPos - endOffset));
            if (newDistance < distance) {
                distance = newDistance;
                insertClass = clazz;
            }

        }
        return insertClass;
    }

    @NotNull
    public SupportedRestMethod mapRestMethod(CreateRequestMapping mainForm) {
        return mainForm.getRbGet().isSelected() ? SupportedRestMethod.GET :
                mainForm.getRbPost().isSelected() ? SupportedRestMethod.POST :
                        mainForm.getRbPut().isSelected() ? SupportedRestMethod.PUT :
                                SupportedRestMethod.DELETE;
    }
}
