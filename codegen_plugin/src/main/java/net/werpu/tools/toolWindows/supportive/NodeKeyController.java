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
