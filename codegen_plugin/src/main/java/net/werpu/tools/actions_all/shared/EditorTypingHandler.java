package net.werpu.tools.actions_all.shared;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * a special handler which calls the registered callbacks upon typing
 * we need this to close subwindows if someone types in the main window
 */
public class EditorTypingHandler implements TypedActionHandler {

    CopyOnWriteArrayList<EditorCallback> callbackList = new CopyOnWriteArrayList<>();

    public void addCallback(EditorCallback callback) {
        if (!callbackList.contains(callback)) {
            callbackList.add(callback);
        }
    }

    public void removeCallback(EditorCallback callback) {
        if (callbackList.contains(callback)) {
            callbackList.remove(callback);
        }
    }


    @Override
    public void execute(@NotNull Editor editor, char c, @NotNull DataContext dataContext) {
        callbackList.stream().forEach(callback -> callback.hasTyped(editor));
        final Document document = editor.getDocument();
        final Project project = editor.getProject();

        WriteCommandAction.runWriteCommandAction(project, () -> insertCharacter(editor, document, c));
    }

    private void insertCharacter(Editor editor, Document document, char c) {
        int selStart = editor.getSelectionModel().getSelectionStart();
        int selEnd = editor.getSelectionModel().getSelectionEnd();
        if (selStart != selEnd) {
            document.replaceString(selStart, selEnd, String.valueOf(c));
        } else {
            CaretModel caretModel = editor.getCaretModel();
            document.insertString(caretModel.getOffset(), String.valueOf(c));
            caretModel.moveToOffset(caretModel.getOffset() + 1);
        }


    }
}
