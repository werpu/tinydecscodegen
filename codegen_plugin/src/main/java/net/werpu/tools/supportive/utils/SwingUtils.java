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

package net.werpu.tools.supportive.utils;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import net.werpu.tools.actions_all.shared.Labels;
import net.werpu.tools.supportive.fs.common.IntellijFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import static net.werpu.tools.supportive.utils.StringUtils.elVis;

public class SwingUtils {


    public static void centerOnParent(final Window child, final boolean absolute) {
        child.pack();
        boolean useChildsOwner = child.getOwner() != null && ((child.getOwner() instanceof JFrame) || (child.getOwner() instanceof JDialog));
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension parentSize = useChildsOwner ? child.getOwner().getSize() : screenSize ;
        final Point parentLocationOnScreen = useChildsOwner ? child.getOwner().getLocationOnScreen() : new Point(0,0) ;
        final Dimension childSize = child.getSize();
        childSize.width = Math.min(childSize.width, screenSize.width);
        childSize.height = Math.min(childSize.height, screenSize.height);
        child.setSize(childSize);
        int x;
        int y;
        if ((child.getOwner() != null) && child.getOwner().isShowing()) {
            x = (parentSize.width - childSize.width) / 2;
            y = (parentSize.height - childSize.height) / 2;
            x += parentLocationOnScreen.x;
            y += parentLocationOnScreen.y;
        } else {
            x = (screenSize.width - childSize.width) / 2;
            y = (screenSize.height - childSize.height) / 2;
        }
        if (!absolute) {
            x /= 2;
            y /= 2;
        }
        child.setLocation(x, y);
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


    @NotNull
    public static Editor createTypescriptEdfitor(Project project, Document document) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(document, project, FileTypeManager.getInstance().getFileTypeByExtension(".ts"), false);

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

    public static void update(JTextField target, JTextField source) {

        String txt = source.getText();

        String name = StringUtils.toLowerDash(txt);
        name = name.replaceAll("\\.+", "/");

        target.setText("/" + name);
    }


    public static void copyToClipboard(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(str), null);
    }

    public static void openEditor(IntellijFileContext foundContext) {
        FileEditor[] editors = (FileEditorManager.getInstance(foundContext.getProject())).openFile(foundContext.getVirtualFile(), true);
        if (editors.length > 0 && elVis(editors[0], Labels.PROP_EDITOR).isPresent()) {
            CaretModel editor = ((Editor) elVis(editors[0], Labels.PROP_EDITOR).get()).getCaretModel();
            editor.moveToOffset(foundContext.getPsiFile().getTextOffset());
            editor.moveCaretRelatively(0, 0, false, false, true);
        }
    }

    public static void openEditor(PsiElementContext foundContext) {
        FileEditor[] editors = (FileEditorManager.getInstance(foundContext.getElement().getProject())).openFile(foundContext.getElement().getContainingFile().getVirtualFile(), true);
        if (editors.length > 0 && elVis(editors[0], Labels.PROP_EDITOR).isPresent()) {
            CaretModel editor = ((Editor) elVis(editors[0], Labels.PROP_EDITOR).get()).getCaretModel();
            //we move to the next possible editing offset
            editor.moveToOffset(foundContext.getTextOffset());
            editor.moveCaretRelatively(0, 0, false, false, true);
        }
    }


    public static ComponentListener addComponentShownHandler(Consumer<ComponentEvent> c) {
        return new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                c.accept(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {
                c.accept(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        };
    }


    public static MouseListener addMouseClickedHandler(Consumer<MouseEvent> singleClick, Consumer<MouseEvent> doubleClick) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.isConsumed()) {
                    return;
                }
                if(!e.isConsumed() && e.getClickCount() > 1) {
                    doubleClick.accept(e);
                }
                singleClick.accept(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }
}
