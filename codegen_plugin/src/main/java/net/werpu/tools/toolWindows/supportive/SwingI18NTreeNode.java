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
 * /
 */

package net.werpu.tools.toolWindows.supportive;

import lombok.Getter;
import net.werpu.tools.supportive.fs.common.I18NElement;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.transformations.i18n.I18NEntry;

import javax.swing.tree.DefaultMutableTreeNode;

public class SwingI18NTreeNode extends DefaultMutableTreeNode {

    @Getter
    I18NFileContext i18NFileContext;

    public SwingI18NTreeNode(I18NElement userObject, I18NFileContext fileContext) {
        super(userObject, !userObject.getSubElements().isEmpty());
        this.i18NFileContext = fileContext;
    }

    @Override
    public String toString() {
        I18NElement userObject = getI18NElement();

        return userObject.getKey();
    }

    public I18NElement getI18NElement() {
        return (I18NElement) getUserObject();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof I18NEntry) {
            return ((I18NElement) obj).getKey().equals(getI18NElement().getKey());
        } else {
            return false;
        }
    }
}
