package net.werpu.tools.actions_all;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import net.werpu.tools.gui.Refactoring;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.transformations.AngularJSComponentTransformationModel;
import net.werpu.tools.supportive.transformations.ComponentTransformation;
import net.werpu.tools.supportive.transformations.IArtifactTransformation;
import net.werpu.tools.supportive.transformations.TransformationDialogBuilder;

import java.awt.*;
import java.io.IOException;

import static com.intellij.ide.scratch.ScratchFileCreationHelper.reformat;

/**
 * action endpoint for the component refactoring dialog
 */
public class RefactorIntoAnnotatedComponent extends AnAction {

    private static final String DIMENSION_KEY = "Compref";
    private static final String DLG_TITLE = "Component Code Proposal";
    private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);


    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        try {
            IntellijFileContext ctx = new IntellijFileContext(e);
            if (!ctx.getText().contains("controller")) {
                AngularJSComponentTransformationModel model = new AngularJSComponentTransformationModel(new IntellijFileContext(e));
                e.getPresentation().setEnabledAndVisible(model.getConstructorBlock().isPresent());
            }
        } catch (Throwable t) {
            e.getPresentation().setEnabledAndVisible(false);
        }


    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        //
        final IntellijFileContext fileContext = new IntellijFileContext(event);
        TransformationDialogBuilder builder = new TransformationDialogBuilder(fileContext, DIMENSION_KEY, DLG_TITLE);
        builder.withTnTransformation((fileContext1, editor, transformation) -> loadTnDecTransformation(fileContext1, editor, transformation));
        builder.withNgTransformation((fileContext1, editor, transformation) -> loadNgTransformation(fileContext1, editor, transformation));
        builder.withModelTransformer((fileContext1, mainForm) -> new ComponentTransformation(new AngularJSComponentTransformationModel(fileContext1)));
        builder.create();

    }

    private boolean okPressed(IntellijFileContext fileContext, Refactoring mainForm) {
        return true;
    }

    public Runnable loadTnDecTransformation(IntellijFileContext fileContext, Editor editor, IArtifactTransformation transformation) {
        Language typeScript = LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));

        return () -> {
            try {
                String text = reformat(fileContext.getProject(), typeScript, transformation.getTnDecTransformation());
                editor.getDocument().setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

    }


    public Runnable loadNgTransformation(IntellijFileContext fileContext, Editor editor, IArtifactTransformation transformation) {
        Language typeScript = LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));

        return () -> {
            try {
                String text = reformat(fileContext.getProject(), typeScript, transformation.getNgTransformation());
                editor.getDocument().setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    @Override
    public boolean isDumbAware() {
        return true;
    }


}