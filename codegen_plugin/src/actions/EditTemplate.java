package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import dtos.ComponentAttribute;
import dtos.ComponentJson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reflector.ComponentAttributesReflector;
import utils.ComponentFileContext;
import utils.IntellijUtils;
import utils.TypescriptFileContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static actions.FormAssertions.assertNotNullOrEmpty;
import static actions.FormAssertions.assertPattern;

public class EditTemplate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        ComponentFileContext fileContext = new ComponentFileContext(e);
        VirtualFile vfile = createWorkFile(fileContext.getProject(), fileContext.getModule());;
        Document document = FileDocumentManager.getInstance().getDocument(vfile);
        final Editor editor = createHtmlEditor(fileContext.getProject(),document);

        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
            editor.getDocument().setText(fileContext.getTemplateText().get());
        });


        DialogWrapper dialogWrapper = new DialogWrapper(fileContext.getProject(), true, DialogWrapper.IdeModalityType.PROJECT) {

            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                return editor.getComponent();
            }

            @Nullable
            @Override
            protected String getDimensionServiceKey() {
                return "AnnComponent";
            }


            @Override
            public void init() {
                super.init();
            }

            public void show() {
                this.init();
                this.setModal(false);
                this.pack();
                super.show();
            }

            @Override
            protected void doOKAction() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                        try {
                            fileContext.setTemplateText(editor.getDocument().getText());
                            fileContext.commit();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } finally {
                            try {
                                vfile.delete(fileContext.getProject());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                });
                super.doOKAction();
            }
        };

        dialogWrapper.setTitle("Create Component");
        dialogWrapper.getWindow().setPreferredSize(new Dimension(400, 300));

        dialogWrapper.show();


    }

    @NotNull
    public static Editor createHtmlEditor(Project project, Document document) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(document, project, FileTypeManager.getInstance().getFileTypeByExtension(".html"), false);

        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setLineMarkerAreaShown(true);
        editorSettings.setLineNumbersShown(true);
        editorSettings.setFoldingOutlineShown(true);
        editorSettings.setAnimatedScrolling(true);
        editorSettings.setWheelFontChangeEnabled(true);
        editorSettings.setVariableInplaceRenameEnabled(true);
        editorSettings.setDndEnabled(true);
        editorSettings.setAutoCodeFoldingEnabled(true);
        editorSettings.setSmartHome(true);

        return editor;
    }

    @Nullable
    private VirtualFile createWorkFile(Project project, Module module) {
        VirtualFile vfile1 = null;
        try {
            File file = FileUtil.createTempFile("edit",".html");
            return LocalFileSystem.getInstance().findFileByPath(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vfile1;
    }

    private void deleteWorkFile(Project project, VirtualFile vfile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                vfile.delete(project);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
