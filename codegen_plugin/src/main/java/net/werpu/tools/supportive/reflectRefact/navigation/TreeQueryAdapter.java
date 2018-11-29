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
