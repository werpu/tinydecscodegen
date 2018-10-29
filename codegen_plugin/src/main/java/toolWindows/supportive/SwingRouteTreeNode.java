package toolWindows.supportive;

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



    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SwingRouteTreeNode) {
            return ((SwingRouteTreeNode) obj).getUserObject().equals(((PsiRouteContext) this.getUserObject()).getRoute());
        } else {
            return false;
        }
    }
}
