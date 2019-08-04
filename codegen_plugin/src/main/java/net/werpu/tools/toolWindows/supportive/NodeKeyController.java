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
import net.werpu.tools.supportive.fs.common.IAngularFileContext;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;


@AllArgsConstructor
public class NodeKeyController<T> implements KeyListener {

    Tree tree;
    Consumer<T> enter;
    Consumer<T> altEnter;
    Consumer<T> copy;


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isBranch()) {
            return;
        }
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        Object value = selectedNode.getUserObject();
        if (!(value instanceof IAngularFileContext)) {
            return;
        }
        if (e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
            altEnter.accept((T) value);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            enter.accept((T) value);
        } else if (e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_C) {
            copy.accept((T) value);
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {

    }


    private boolean isBranch() {
        return !(tree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) ||
                !((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).isLeaf();
    }
}
