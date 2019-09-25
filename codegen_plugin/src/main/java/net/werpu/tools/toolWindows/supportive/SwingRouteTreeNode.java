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

import net.werpu.tools.supportive.fs.common.PsiRouteContext;

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
        if (obj instanceof SwingRouteTreeNode) {
            return ((SwingRouteTreeNode) obj).getUserObject().equals(((PsiRouteContext) this.getUserObject()).getRoute());
        } else {
            return false;
        }
    }
}
