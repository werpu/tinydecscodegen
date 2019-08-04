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

public interface TreeQueryAdapter<T> {

    /**
     * search only the next child elements
     */
    List<T> findChildren(T ctx, Function<T, Boolean> visitor);

    /**
     * find elements, find all elements, which match the visitor function
     * (aka search recursively from the current element downwards into the deepest
     * levels of the tree
     *
     */
    List<T> findElements(T ctx, Function<T, Boolean> visitor);




    List<T> findNextSiblings(T cty, Function<T, Boolean> visitor);

    List<T> findPrevSiblings(T cty, Function<T, Boolean> visitor);

    /**
     * parents walker
     *
     * find all parents which match the visitor function
     */
    List<T> walkParents(T ctx, Function<T, Boolean> visitor);

    /**
     * textual representation (most of the times the entire content,
     * can be the same as the identifier but not necessarily so
     * in an AST for instance this is the entire textual representation
     * of the subtree below and including the node)
     */
    String getText(T ctx);

    String getUnquotedText(PsiElementContext ctx);

    /**
     * non unique string representation of
     * the elements identifier
     * (can be for instance in an AST the node type like "IF_STATEMENT")
     */
    String getIdentifier(T ctx);

    /**
     * toString for the current tree node
     */
    String toString(T ctx);
}
