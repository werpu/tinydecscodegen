package toolWindows.supportive;

import com.intellij.ui.treeStructure.Tree;
import lombok.AllArgsConstructor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;


@AllArgsConstructor
public class NodeKeyController<T> implements KeyListener {

    Tree tree;
    Consumer<T> goToCode;
    Consumer<T> goToRegistration;
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
        if (!isInstanceOfT(value)) return;
        if (e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                goToCode.accept((T) value);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                goToRegistration.accept((T) value);
        } else if (e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_C) {
                copy.accept((T) value);
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {

    }

    public boolean isInstanceOfT(Object userObj) {
        try {
           T letItFail = (T) userObj;
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    private boolean isBranch() {
        return !(tree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode);
    }
}
