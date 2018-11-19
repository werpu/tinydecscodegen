package net.werpu.tools.supportive.transformations;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import lombok.RequiredArgsConstructor;
import net.werpu.tools.actions_all.shared.TransformationInvoker;
import net.werpu.tools.gui.support.InputDialogWrapperBuilder;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.utils.SwingUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Function;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;

/**
 * a helper class to isolate the common dialog behavior
 * for all transformations
 */
@RequiredArgsConstructor
public class TransformationDialogBuilder {
    private final IntellijFileContext fileContext;
    private final String titleKey;
    private final String title;
    private boolean isModule = true;


    TransformationInvoker invokeTnTransformation = (fileContext, editor, model) -> () -> {};
    TransformationInvoker invokeNgTnTransformation = (fileContext, editor, model) -> () -> {};
    Function<ActionEvent, Boolean> okPressed = (ActionEvent ev) -> Boolean.TRUE;
    Function<ActionEvent, Boolean> cancelPressed = (ActionEvent ev) -> Boolean.TRUE;

    Dimension defaultDimnension = new Dimension(800, 600);

    public ITransformationCreator modelTransformer = (fileContext1, mainForm) -> {
        AngularJSModuleTransformationModel transformationModel = new AngularJSModuleTransformationModel(fileContext1);
        transformationModel.setApplicationBootstrap(mainForm.getCbBootstrap().isSelected());
        ModuleTransformation moduleTransformation = new ModuleTransformation(transformationModel);
        return moduleTransformation;
    };

    public TransformationDialogBuilder withNgTransformation(TransformationInvoker invoker) {
        invokeNgTnTransformation = invoker;
        return this;
    }
    public TransformationDialogBuilder withTnTransformation(TransformationInvoker invoker) {
        invokeTnTransformation = invoker;
        return this;
    }

    public TransformationDialogBuilder withOkPressed(Function<ActionEvent, Boolean> func) {
        this.okPressed = func;
        return this;
    }

    public TransformationDialogBuilder withCancelPressed(Function<ActionEvent, Boolean> func) {
        this.cancelPressed = func;
        return this;
    }

    public TransformationDialogBuilder withDimension(Dimension dim) {
        this.defaultDimnension = dim;
        return this;
    }

    public TransformationDialogBuilder withModelTransformer(ITransformationCreator func) {
        this.modelTransformer = func;
        return this;
    }

    public TransformationDialogBuilder withIsModule(boolean module) {
        this.isModule = module;
        return this;
    }

    public void create() {
        net.werpu.tools.gui.Refactoring mainForm = new net.werpu.tools.gui.Refactoring();
        runWriteCommandAction(fileContext.getProject(), () -> {

            Language typeScript = LanguageUtil.getFileTypeLanguage(FileTypeManager.getInstance().getStdFileType("TypeScript"));
            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText("newModule.ts",
                    typeScript, "");

            Document document = workFile.getViewProvider().getDocument();
            final Editor editor = SwingUtils.createTypescriptEdfitor(fileContext.getProject(), document);
            editor.getDocument().setText("  ");

            getApplication().invokeLater(() -> {
                DialogWrapper dialogWrapper = new InputDialogWrapperBuilder(fileContext.getProject(), mainForm.getRootPanel())
                        .withDimensionKey(titleKey)
                        .withTitle(title)
                        .withPreferredSize(defaultDimnension)
                        .withOkHandler(() -> true)
                        .create();


                ModuleTransformation moduleTransformation = (ModuleTransformation) modelTransformer.apply(fileContext, mainForm);


                mainForm.getEditorScroll().getViewport().setView(editor.getComponent());

                mainForm.onTnDecSelected(() -> runWriteCommandAction(fileContext.getProject(), invokeTnTransformation.createAction(fileContext, editor, moduleTransformation)));
                mainForm.onbNgSelected(() -> runWriteCommandAction(fileContext.getProject(), invokeNgTnTransformation.createAction(fileContext, editor, moduleTransformation)));
                mainForm.onStartupModuleChange((isSelected) -> {
                    moduleTransformation.getTransformationModel().setApplicationBootstrap(isSelected);
                    if(mainForm.getRbNg().isSelected()) {
                        runWriteCommandAction(fileContext.getProject(), invokeNgTnTransformation.createAction(fileContext, editor, moduleTransformation));
                    } else {
                        runWriteCommandAction(fileContext.getProject(), invokeTnTransformation.createAction(fileContext, editor, moduleTransformation));
                    }
                });
                runWriteCommandAction(fileContext.getProject(), invokeTnTransformation.createAction(fileContext, editor, moduleTransformation));

                dialogWrapper.show();

            });
        });
    }


}
