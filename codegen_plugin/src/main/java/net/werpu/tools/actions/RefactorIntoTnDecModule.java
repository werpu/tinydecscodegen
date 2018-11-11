package net.werpu.tools.actions;

import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.SwingUtils;

import java.awt.*;

/**
 * Refactor module into a new TNDecModule
 */
public class RefactorIntoTnDecModule extends AnAction {

    private static final String DIMENSION_KEY = "AnnRef";
    private static final String DLG_TITLE = "Refactor Module";
    private static final Dimension PREFERRED_SIZE = new Dimension(400, 300);

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        net.werpu.tools.gui.CreateTnDecComponent mainForm = new net.werpu.tools.gui.CreateTnDecComponent();
        final IntellijFileContext fileContext = new IntellijFileContext(event);
        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText("newModule.ts",
                    LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript")),"");

            Document document = workFile.getViewProvider().getDocument();
            final Editor editor = SwingUtils.createHtmlEditor(fileContext.getProject(), document);
            editor.getDocument().setText("  ");

            ApplicationManager.getApplication().invokeLater(() -> {
                DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(fileContext.getProject(), mainForm.getRootPanel())
                        .withDimensionKey(DIMENSION_KEY)
                        .withTitle(DLG_TITLE)
                        .withPreferredSize(PREFERRED_SIZE)
                        .withOkHandler(() -> okPressed(fileContext, mainForm))
                        .create();

                dialogWrapper.show();

            });
        });
    }

    public boolean okPressed(IntellijFileContext fileContext, net.werpu.tools.gui.CreateTnDecComponent mainForm) {
        return true;
    }


    @Override
    public boolean isDumbAware() {
        return true;
    }
}
