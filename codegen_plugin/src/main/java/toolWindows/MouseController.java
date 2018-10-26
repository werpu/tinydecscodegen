package toolWindows;

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

    public void doMouseClicked(MouseEvent ev) {

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

    public boolean isInstanceOfT(Object userObj) {
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
