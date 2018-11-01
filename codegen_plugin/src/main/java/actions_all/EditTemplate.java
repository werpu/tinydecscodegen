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
package actions_all;

import actions_all.shared.EditorCallback;
import actions_all.shared.Messages;
import actions_all.shared.VisibleAssertions;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorTabbedContainer;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.IntellijFileContext;
import supportive.utils.IntellijUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static supportive.utils.IntellijRunUtils.smartInvokeLater;
import static supportive.utils.SwingUtils.createHtmlEditor;


public class EditTemplate extends AnAction implements EditorCallback {


    public static final String TEMPLATE_OF = "Template of: ";

    public void update(AnActionEvent anActionEvent) {
        IntellijFileContext ctx = new IntellijFileContext(anActionEvent);
        if (VisibleAssertions.assertNotTs(ctx) ||
                !VisibleAssertions.assertTemplated(ctx)) {
            anActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }


        anActionEvent.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void hasTyped(Editor editor) {


    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // handler.addCallback(this);

        ComponentFileContext fileContext = new ComponentFileContext(e);


        if (!fileContext.getTemplateTextAsStr().isPresent()) {
            supportive.utils.IntellijUtils.showErrorDialog(fileContext.getProject(), "No template string could be found", Messages.ERR_OCCURRED);
            return;
        }
        final Editor ediOrig = IntellijUtils.getEditor(e);


        WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {

            //We basically do the same as the intellij fragment editor
            //I could not find any decent docs how to do that
            //so here is what I do, I create a double buffer document
            //with the correct language
            //push the psi element text in
            //and on every doc update I update the original editor document
            //intellij handles the rest
            String title = TEMPLATE_OF + fileContext.getVirtualFile().getName();
            PsiFile workFile = PsiFileFactory.getInstance(fileContext.getProject()).createFileFromText(title,
                    HTMLLanguage.INSTANCE, "");


            final RetVal htmlEditContext = createDoubleBuffer(fileContext, workFile);
            final Document doubleBuffer = htmlEditContext.getDocument();
            final FileEditorManagerEx edManager = (FileEditorManagerEx) FileEditorManagerEx.getInstance(fileContext.getProject());

            EditorWindow currentWindow = edManager.getCurrentWindow();
            EditorsSplitters splitters = FileEditorManagerEx.getInstanceEx(fileContext.getProject()).getSplitters();
            final VirtualFile virtualFile = workFile.getVirtualFile();

            if(!currentWindow.inSplitter()) {
                edManager.createSplitter(SwingConstants.VERTICAL, currentWindow);


                smartInvokeLater(fileContext.getProject(), () -> {
                    recalculatedNewWindowPos(currentWindow, splitters);
                    FileEditor[] editors = FileEditorManager.getInstance(fileContext.getProject()).openFile(virtualFile, true, true);
                    closeOldEditor(fileContext.getProject(), title);
                });

            } else {
                recalculatedNewWindowPos(currentWindow, splitters);
                smartInvokeLater(fileContext.getProject(), () -> {
                    FileEditor[] editors = FileEditorManager.getInstance(fileContext.getProject()).openFile(virtualFile, true, true);
                });
            }




            //ComponentFileContext fileContext2 = new ComponentFileContext(e.getProject(), fileEditor[0].getFile());

            ApplicationManager.getApplication().invokeLater(() -> {
                    appendBehavior(fileContext, ediOrig, title, doubleBuffer);
            });

        });
    }

    private void recalculatedNewWindowPos(EditorWindow currentWindow, EditorsSplitters splitters) {
        int pos = 0;
        int cnt = 0;
        for(EditorWindow w : splitters.getWindows()) {
            if(w == currentWindow) {
                pos = cnt;
                break;
            }
            cnt++;
        }
        if(pos != splitters.getWindows().length - 1) {
            pos = pos + 1;
        } else {
            pos = pos - 1;
        }

        splitters.getWindows()[pos].setAsCurrentWindow(true);
    }

    //https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics/editor_events.html
    public void appendBehavior(ComponentFileContext fileContext, Editor ediOrig, String title, Document doubleBuffer) {
        DocumentListener closeListener = newCloseListener(fileContext, ediOrig, title);

        doubleBuffer.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                //ApplicationManager.getApplication().invokeLater(() -> {


                WriteCommandAction.runWriteCommandAction(fileContext.getProject(), () -> {
                    UndoManager undoManager = UndoManagerImpl.getInstance(fileContext.getProject());
                    if (undoManager.isUndoInProgress() || undoManager.isRedoInProgress()) {
                        return;
                    }
                    ediOrig.getDocument().removeDocumentListener(closeListener);
                    fileContext.directUpdateTemplate(event.getDocument().getText());
                    ediOrig.getDocument().addDocumentListener(closeListener);

                });
            }
        });
        ediOrig.getDocument().addDocumentListener(closeListener);
    }

    void executeOntabs(Project project, String title, TabExecutor executor) {
        final FileEditorManagerEx edManager = (FileEditorManagerEx) FileEditorManagerEx.getInstance(project);

        Arrays.stream(edManager.getWindows()).forEach(ed -> {
            EditorTabbedContainer tabbedPane = ed.getTabbedPane();
            JBTabs tabs = tabbedPane.getTabs();
            int editorPos = -1;
            int htmlPos = -1;
            for (int cnt = 0; cnt < tabs.getTabCount() && (editorPos == -1 || htmlPos == -1); cnt++) {
                TabInfo tab = tabs.getTabAt(cnt);
                if (tab.getText().equalsIgnoreCase(title.substring(TEMPLATE_OF.length()))) {
                    editorPos = cnt;
                }
                if(tab.getText().equalsIgnoreCase(title)) {
                    htmlPos = cnt;
                }
            }
            if(editorPos != -1 || htmlPos != -1) {
                 executor.apply(editorPos, htmlPos, ed, tabbedPane);

            }
        });
    }


    void closeOldEditor(Project project, String title) {

        executeOntabs(project, title, (editorPos, htmlPos, ed, tabbedPane) -> {
            if(editorPos != -1 && htmlPos != -1) {
                tabbedPane.removeTabAt(editorPos, (htmlPos < editorPos) ? htmlPos : htmlPos - 1);
            }
        });

    }


    @NotNull
    public DocumentListener newCloseListener(ComponentFileContext fileContext, Editor ediOrig, String title) {
        return new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {

            }

            @Override
            public void documentChanged(DocumentEvent event) {


                if (fileContext.inTemplate(event.getOffset())) {
                    //Arrays.stream(editors).forEach(editor -> editor.dispose());
                    Project project = fileContext.getProject();
                    final FileEditorManagerEx edManager = (FileEditorManagerEx) FileEditorManagerEx.getInstance(project);
                    final EditorWindow activeWindow;
                    try {
                        activeWindow = edManager.getActiveWindow().blockingGet(1000);
                        if (activeWindow.getSelectedFile().getName().equals(title)) {
                            return;
                        }
                    } catch (TimeoutException | ExecutionException e) {
                        e.printStackTrace();
                        return;
                    }

                    executeOntabs(project, title, (editorPos, htmlPos, ed, tabbedPane) -> {
                        if(htmlPos != -1) {
                            if (tabbedPane.getTabCount() == 1) {
                                ed.closeFile(ed.getSelectedFile());
                            } else {
                                tabbedPane.removeTabAt(htmlPos, Math.max(0,(editorPos != -1)? editorPos : htmlPos-1));
                            }
                            ediOrig.getDocument().removeDocumentListener(this);
                        }

                    });

                }
            }
        };
    }

    @NotNull
    private RetVal createDoubleBuffer(ComponentFileContext fileContext, PsiFile workFile) {
        Document document = workFile.getViewProvider().getDocument();
        Editor editor = createHtmlEditor(fileContext.getProject(), document);
        Document doubleBuffer = editor.getDocument();
        doubleBuffer.setText(fileContext.getTemplateTextAsStr().get());
        return new RetVal(doubleBuffer, editor);
    }

    @Getter
    @AllArgsConstructor
    class RetVal {
        private Document document;
        private Editor editor;
    }

    interface TabExecutor {
        void apply(int editorPos, int htmlEditorPos,EditorWindow ed, EditorTabbedContainer tabbedPane);
    }

}
