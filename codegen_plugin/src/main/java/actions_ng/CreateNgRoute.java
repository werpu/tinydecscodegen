package actions_ng;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.IntellijFileContext;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CreateNgRoute extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {

        IntellijFileContext fileContext = new IntellijFileContext(event);

        final gui.CreateRoute mainForm = new gui.CreateRoute();

        DialogWrapper dialogWrapper = new DialogWrapper(fileContext.getProject(), true, DialogWrapper.IdeModalityType.PROJECT) {

            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                return mainForm.getRootPanel();
            }

            @Nullable
            @Override
            protected String getDimensionServiceKey() {
                return "AnnRoute";
            }

            @Nullable
            @NotNull
            protected List<ValidationInfo> doValidateAll() {
                /*return Arrays.asList(
                        assertNotNullOrEmpty(mainForm.getName(), Messages.ERR_NAME_VALUE, mainForm.getTxtName()),
                        assertPattern(mainForm.getName(), VALID_NAME, Messages.ERR_CONFIG_PATTERN, mainForm.getTxtName())
                ).stream().filter(s -> s != null).collect(Collectors.toList());*/
                return Collections.emptyList();
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

        dialogWrapper.setTitle("Create Route");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));
        dialogWrapper.show();


        if(dialogWrapper.isOK()) {
            //TODO perform refactoring operation here
        }

    }

    //List<IntellijFileContext> getPossibleComponentCandidates(IntellijFileContext rootContext) {
    //List<PsiFile> foundFiles = IntellijUtils.searchFiles(rootContext.getProject(), "ts", "@Component").stream()
    //            .filter(psiFile -> psiFile.getVirtualFile().getPath().replaceAll("\\\\", "/").contains("/pages/"))
    //            .collect(Collectors.toList());

        //now we have found the files we need to parse the class names, we have to search for the component annotation and then step
        //down from there to the first class definition
        //foundFiles.stream().map(el -> new PsiElementContext(el))
        //        .flatMap(elc -> elc.findPsiElement(el -> æ))

    //}
}
