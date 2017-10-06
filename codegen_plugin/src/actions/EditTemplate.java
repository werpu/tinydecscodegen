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

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diff.impl.incrementalMerge.ui.EditorPlace;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorTabbedContainer;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import utils.ComponentFileContext;
import utils.IntellijUtils;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;

public class EditTemplate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        ComponentFileContext fileContext = new ComponentFileContext(e);


        if (!fileContext.getTemplateTextAsStr().isPresent()) {
            com.intellij.openapi.ui.Messages.showErrorDialog(fileContext.getProject(), "No template string could be found", actions.Messages.ERR_OCCURRED);
            return;
        }


        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            //We basically do the same as the intellij fragment editor
            //I could not find any decent docs how to do that
            //so here is what I do, I create a double buffer document
            //with the correct language
            //push the psi element text in
            //and on every doc update I update the original editor document
            //intellij handles the rest
            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText("Template of: " + fileContext.getVirtualFile().getName(),
                    HTMLLanguage.INSTANCE, "");


             Document doubleBuffer = createDoubleBuffer(fileContext, workFile);
            final FileEditorManagerEx edManager = (FileEditorManagerEx) FileEditorManagerEx.getInstance(fileContext.getProject());

            EditorWindow currentWindow = edManager.getCurrentWindow();
            edManager.createSplitter(SwingConstants.HORIZONTAL, currentWindow);
            final VirtualFile virtualFile = workFile.getVirtualFile();
            FileEditorManager.getInstance(fileContext.getProject()).openFile(virtualFile, true);
            currentWindow.getTabbedPane().close();

           /*

            DocumentListener dl = new DocumentListener() {
                @Override
                public void documentChanged(DocumentEvent event) {
                    WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                        Arrays.stream(edManager.getWindows()).filter(ed -> {
                            EditorTabbedContainer tabbedPane = ed.getTabbedPane();
                            return (tabbedPane.getTabs().getTargetInfo().getText().contains("Template of: " + fileContext.getVirtualFile().getName()));
                        }).forEach(ed -> {
                            ed.closeFile(virtualFile);
                            //ed.getTabbedPane().close();
                        });
                        fileContext.getDocument().removeDocumentListener(this);
                    });
                }
            };

            editor.getComponent().getComponent(1).addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    fileContext.getDocument().addDocumentListener(dl);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    fileContext.getDocument().removeDocumentListener(dl);
                }
            });
*/

            doubleBuffer.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(DocumentEvent event) {
                    fileContext.directUpdateTemplate(event.getDocument().getText());
                }
            });


        });
    }

    @NotNull
    private Document createDoubleBuffer(ComponentFileContext fileContext, PsiFile workFile) {
        Document document = workFile.getViewProvider().getDocument();
        Editor editor = createHtmlEditor(fileContext.getProject(), document);
        Document doubleBuffer = editor.getDocument();
        doubleBuffer.setText(fileContext.getTemplateTextAsStr().get());
        return doubleBuffer;
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

}
