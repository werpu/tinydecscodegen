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

package net.werpu.tools.supportive.reflectRefact.navigation;

import net.werpu.tools.supportive.fs.common.PsiElementContext;

import java.util.List;
import java.util.function.Function;

public class PsiElementQueryAdapter implements TreeQueryAdapter<PsiElementContext> {


    /**
     * find an element among the direct children
     */
    @Override
    public List<PsiElementContext> findChildren(PsiElementContext ctx, Function<PsiElementContext, Boolean> visitor) {
        return ctx.getChildren(visitor);
    }

    /**
     * recursive search deep down finding all elements
     */
    @Override
    public List<PsiElementContext> findElements(PsiElementContext ctx, Function<PsiElementContext, Boolean> visitor) {
        return ctx.findPsiElements2(visitor);
    }

    @Override
    public List<PsiElementContext> findNextSiblings(PsiElementContext cty, Function<PsiElementContext, Boolean> visitor) {
        return cty.walkNextSiblings(visitor);
    }

    @Override
    public List<PsiElementContext> findPrevSiblings(PsiElementContext cty, Function<PsiElementContext, Boolean> visitor) {
       return cty.walkPrevSiblings(visitor);
    }

    /**
     * recursive search up finding all matching elements
     */
    @Override
    public List<PsiElementContext> walkParents(PsiElementContext ctx, Function<PsiElementContext, Boolean> visitor) {
        return ctx.walkParents(visitor);
    }

    @Override
    public String getText(PsiElementContext ctx) {
        return ctx.getText();
    }

    @Override
    public String getUnquotedText(PsiElementContext ctx) {
        return ctx.getUnquotedText();
    }

    @Override
    public String getIdentifier(PsiElementContext ctx) {
        return ctx.getName();
    }

    @Override
    public String toString(PsiElementContext ctx) {
        return ctx.getElement().toString();
    }


}
