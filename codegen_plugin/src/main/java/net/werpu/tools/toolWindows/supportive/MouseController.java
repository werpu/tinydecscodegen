/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.toolWindows.supportive;

import com.intellij.ui.treeStructure.Tree;
import lombok.AllArgsConstructor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@AllArgsConstructor
public class MouseController<T> implements MouseListener {

    Tree tree;
    ClickHandler<T, MouseEvent> rightMouseButton;

    private void doMouseClicked(MouseEvent ev) {

        TreePath tp = tree.getPathForLocation(ev.getX(), ev.getY());
        if (tp == null) {
            return;
        }
        Object selectedNode = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
        if (selectedNode == null) {
            return;
        }
        if (!isInstanceOfT(selectedNode)) {
            return;
        }


        if (SwingUtilities.isRightMouseButton(ev)) {
            rightMouseButton.accept((T) selectedNode, ev);
        }


    }

    private boolean isInstanceOfT(Object userObj) {
        try {
            T letItFail = (T) userObj;
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        doMouseClicked(e);
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
}
