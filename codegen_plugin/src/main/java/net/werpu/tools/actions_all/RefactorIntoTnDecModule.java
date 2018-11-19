package net.werpu.tools.actions_all;

import com.intellij.lang.Language;
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
import net.werpu.tools.actions_all.shared.TransformationInvoker;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSModuleTransformationModel;
import net.werpu.tools.supportive.transformations.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.ModuleTransformation;
import net.werpu.tools.supportive.transformations.TransformationDialogBuilder;
import net.werpu.tools.supportive.utils.SwingUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

import static com.intellij.ide.scratch.ScratchFileCreationHelper.reformat;
import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;

/**
 * Refactor module into a new TNDecModule
 */
public class RefactorIntoTnDecModule extends AnAction {

    private static final String DIMENSION_KEY = "AnnRef";
    private static final String DLG_TITLE = "Module Code Proposal";
    private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        try {
            IntellijFileContext ctx = new IntellijFileContext(e);
            if(!ctx.getText().contains(".module")) {
                AngularJSModuleTransformationModel module = new AngularJSModuleTransformationModel(e);
                e.getPresentation().setEnabledAndVisible(module.getModuleDeclStart().isPresent());
            }
        } catch (Throwable t) {
            e.getPresentation().setEnabledAndVisible(false);
        }


    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        net.werpu.tools.gui.Refactoring mainForm = new net.werpu.tools.gui.Refactoring();
        final IntellijFileContext fileContext = new IntellijFileContext(event);
        TransformationDialogBuilder builder = new TransformationDialogBuilder(fileContext, DIMENSION_KEY,  DLG_TITLE);
        builder.withTnTransformation((fileContext1, editor, model) -> loadTnDecTransformation(fileContext1, editor, (ModuleTransformation) model));
        builder.withNgTransformation((fileContext1, editor, model) -> loadNgTransformation(fileContext1, editor, (ModuleTransformation) model));
        builder.create();
    }




    @NotNull
    public Runnable loadTnDecTransformation(IntellijFileContext fileContext, Editor editor, ModuleTransformation moduleTransformation) {
        Language typeScript = LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));

        return () -> {
            try {
                String text = reformat(fileContext.getProject(), typeScript,  moduleTransformation.getTnDecTransformation());
                editor.getDocument().setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    @NotNull
    public Runnable loadNgTransformation(IntellijFileContext fileContext, Editor editor, ModuleTransformation moduleTransformation) {
        Language typeScript = LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));
        return () -> {
            try {
                String text = reformat(fileContext.getProject(), typeScript,  moduleTransformation.getNgTransformation());
                editor.getDocument().setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean okPressed(IntellijFileContext fileContext, net.werpu.tools.gui.Refactoring mainForm) {
        return true;
    }


    @Override
    public boolean isDumbAware() {
        return true;
    }
}
