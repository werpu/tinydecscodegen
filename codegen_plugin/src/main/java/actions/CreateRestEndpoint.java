package actions;

import actions.shared.JavaFileContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiElement;
import com.jgoodies.common.base.Strings;
import gui.CreateRequestMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.IntellijFileContext;
import utils.IntellijUtils;
import utils.PsiWalkFunctions;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static actions.FormAssertions.assertNotNullOrEmpty;

enum SupportedRestMethod {
    PUT, GET, DELETE, POST
}

public class CreateRestEndpoint extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent event) {

        if (event.getData(PlatformDataKeys.EDITOR) == null) {
            PopupUtil.showBalloonForActiveFrame("No editor found, please focus on an open source file", MessageType.ERROR);
            return;
        }
        JavaFileContext javaData = new JavaFileContext(event);
        if (javaData.isError()) return;


        final gui.CreateRequestMapping mainForm = new gui.CreateRequestMapping();


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

            String returnType = (String) mainForm.getCbReturnType().getSelectedItem();
            returnType = (Strings.isBlank(returnType)) ? "void" : returnType;
            boolean isList = mainForm.getCbList().isSelected();
            boolean createMapping = mainForm.getCbTypeScript().isSelected();

            //now lets insert the code first

            Editor editor = IntellijUtils.getEditor(event);
            IntellijFileContext editorFile = new IntellijFileContext(event);
            final int cursorPos = editor.getCaretModel().getOffset();

            Optional<PsiElement> after = editorFile.findPsiElements(PsiWalkFunctions::isMethod)
                    .stream()
                    .filter(el -> el.getTextOffset() >= cursorPos).findFirst();
            List<PsiElement> before = editorFile.findPsiElements(PsiWalkFunctions::isMethod)
                    .stream()
                    .filter(el -> el.getTextOffset() <= cursorPos).collect(Collectors.toList());

            PsiElement beforeElement = (before.size() > 0) ? before.get(before.size() - 1) : null;


            WriteCommandAction.runWriteCommandAction(editorFile.getProject(), () -> {

                if (after.isPresent() && beforeElement != null &&
                        beforeElement.getTextRange().getEndOffset() <= cursorPos &&
                        after.get().getTextRange().getStartOffset() >= cursorPos
                        ) {
                    editor.getDocument().insertString(cursorPos, "Hello world from me pos");
                } else if (after.isPresent()) {
                    editor.getDocument().insertString(after.get().getTextRange().getStartOffset(), "Hello world from me");
                } else if (beforeElement != null) {

                    editor.getDocument().insertString(beforeElement.getTextRange().getEndOffset() + 1, "Hello world from me after");
                } else {
                    editor.getDocument().insertString(cursorPos, "Hello world from me other");

                }
            });


        }

    }

    @NotNull
    public SupportedRestMethod mapRestMethod(CreateRequestMapping mainForm) {
        return mainForm.getRbGet().isSelected() ? SupportedRestMethod.GET :
                mainForm.getRbPost().isSelected() ? SupportedRestMethod.POST :
                        mainForm.getRbPut().isSelected() ? SupportedRestMethod.PUT :
                                SupportedRestMethod.DELETE;
    }
}
