/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package actions;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ComponentFileContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class EditTemplate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        ComponentFileContext fileContext = new ComponentFileContext(e);


        if (!fileContext.getTemplateText().isPresent()) {
            com.intellij.openapi.ui.Messages.showErrorDialog(fileContext.getProject(), "No template string could be found", actions.Messages.ERR_OCCURRED);
            return;
        }


        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText("create.html",
                    HTMLLanguage.INSTANCE, "");

            Document document = workFile.getViewProvider().getDocument();
            Editor editor = createHtmlEditor(fileContext.getProject(), document);
            editor.getDocument().setText(fileContext.getTemplateText().get());

            ApplicationManager.getApplication().invokeLater(() -> {
                showDialog(fileContext, editor);
            });
        });
    }

    private void showDialog(ComponentFileContext fileContext, Editor editor) {
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
                        }
                    });
                });
                super.doOKAction();
            }
        };

        dialogWrapper.setTitle("Edit Template");
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
    private VirtualFile createWorkFile(File file) {

        return LocalFileSystem.getInstance().findFileByIoFile(file);

    }

}
