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

import net.werpu.tools.supportive.fs.common.I18NElement;
import net.werpu.tools.supportive.fs.common.I18NFileContext;
import net.werpu.tools.supportive.fs.common.PsiElementContext;
import net.werpu.tools.supportive.fs.common.PsiI18nEntryContext;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree factory to build the i18n tree model
 */
public class SwingI18NTreeFactory {
    public static DefaultMutableTreeNode createRouteTrees(I18NFileContext ctx, String label) {

        PsiI18nEntryContext entryContext = Arrays.stream(new I18NFileContext[]{ctx})
                .map(fileContext -> new PsiElementContext(fileContext.getPsiFile()))
                .map(psiElementContext -> new PsiI18nEntryContext(psiElementContext)).findFirst().get();

        Map<String, DefaultMutableTreeNode> _routeIdx = new HashMap<>();
        DefaultMutableTreeNode rootParentNode = new SwingRootParentNode(label);
        DefaultMutableTreeNode parNode = rootParentNode;

        _routeIdx.put("", parNode);

        List<I18NElement> elementList = entryContext.getRootTreeReference().getSubElements();
        applyBranch(ctx, parNode, elementList);

        return rootParentNode;
    }

    public static void applyBranch(I18NFileContext ctx, DefaultMutableTreeNode parNode, List<I18NElement> elementList) {
        for (I18NElement element : elementList) {

            boolean isBranch = !element.getSubElements().isEmpty();
            SwingI18NTreeNode treeNode = new SwingI18NTreeNode(element, ctx);
            parNode.add(treeNode);
            if (isBranch) {
                SwingI18NTreeFactory.applyBranch(ctx, treeNode, element.getSubElements());
            }
        }
    }
}
