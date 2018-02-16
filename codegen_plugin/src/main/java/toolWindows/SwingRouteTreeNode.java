package toolWindows;

import supportive.fs.common.PsiRouteContext;

import javax.swing.tree.DefaultMutableTreeNode;

public class SwingRouteTreeNode extends DefaultMutableTreeNode {

    public SwingRouteTreeNode(PsiRouteContext userObject) {
        super(userObject);

    }

    public SwingRouteTreeNode(PsiRouteContext userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    @Override
    public String toString() {
        PsiRouteContext userObject = (PsiRouteContext) getUserObject();
        return userObject.getRoute().getRouteKey();
    }
}
