package net.werpu.tools.supportive.refactor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * helper element to enforce an insert instead of a replace by proxiying
 * a position in the document
 */
public class DummyInsertPsiElement extends DummyReplacePsiElement {

    public DummyInsertPsiElement(int pos) {
        super(pos, 0);
    }
}
