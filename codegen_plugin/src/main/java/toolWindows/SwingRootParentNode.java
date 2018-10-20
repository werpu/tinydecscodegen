package toolWindows;

import javax.swing.tree.DefaultMutableTreeNode;

public class SwingRootParentNode extends DefaultMutableTreeNode {

    public SwingRootParentNode() {
        super();
    }

    public SwingRootParentNode(Object userObject) {
        super(userObject);
    }

    public SwingRootParentNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DefaultMutableTreeNode) {
            return this.getUserObject().equals(((DefaultMutableTreeNode) obj).getUserObject());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getUserObject().hashCode();
    }
}
