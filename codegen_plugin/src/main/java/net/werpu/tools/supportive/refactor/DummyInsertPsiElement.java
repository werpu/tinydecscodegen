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
public class DummyInsertPsiElement implements PsiElement {

    int pos = 0;


    public DummyInsertPsiElement(int pos) {
        this.pos = pos;
    }

    @NotNull
    @Override
    public Project getProject() throws PsiInvalidElementAccessException {
        return null;
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return null;
    }

    @Override
    public PsiManager getManager() {
        return null;
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return new PsiElement[0];
    }

    @Override
    public PsiElement getParent() {
        return null;
    }

    @Override
    public PsiElement getFirstChild() {
        return null;
    }

    @Override
    public PsiElement getLastChild() {
        return null;
    }

    @Override
    public PsiElement getNextSibling() {
        return null;
    }

    @Override
    public PsiElement getPrevSibling() {
        return null;
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return null;
    }

    @Override
    public TextRange getTextRange() {
        return new TextRange(pos,pos);
    }

    @Override
    public int getStartOffsetInParent() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Nullable
    @Override
    public PsiElement findElementAt(int i) {
        return null;
    }

    @Nullable
    @Override
    public PsiReference findReferenceAt(int i) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return pos;
    }

    @Override
    public String getText() {
        return "";
    }

    @NotNull
    @Override
    public char[] textToCharArray() {
        return "".toCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return null;
    }

    @Override
    public PsiElement getOriginalElement() {
        return null;
    }

    @Override
    public boolean textMatches(@NotNull CharSequence charSequence) {
        return false;
    }

    @Override
    public boolean textMatches(@NotNull PsiElement psiElement) {
        return false;
    }

    @Override
    public boolean textContains(char c) {
        return false;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor psiElementVisitor) {

    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor psiElementVisitor) {

    }

    @Override
    public PsiElement copy() {
        return null;
    }

    @Override
    public PsiElement add(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement psiElement, @Nullable PsiElement psiElement1) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement psiElement, @Nullable PsiElement psiElement1) throws IncorrectOperationException {
        return null;
    }

    @Override
    public void checkAdd(@NotNull PsiElement psiElement) throws IncorrectOperationException {

    }

    @Override
    public PsiElement addRange(PsiElement psiElement, PsiElement psiElement1) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement psiElement, @NotNull PsiElement psiElement1, PsiElement psiElement2) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addRangeAfter(PsiElement psiElement, PsiElement psiElement1, PsiElement psiElement2) throws IncorrectOperationException {
        return null;
    }

    @Override
    public void delete() throws IncorrectOperationException {

    }

    @Override
    public void checkDelete() throws IncorrectOperationException {

    }

    @Override
    public void deleteChildRange(PsiElement psiElement, PsiElement psiElement1) throws IncorrectOperationException {

    }

    @Override
    public PsiElement replace(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return null;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new PsiReference[0];
    }

    @Nullable
    @Override
    public <T> T getCopyableUserData(Key<T> key) {
        return null;
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T t) {

    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull ResolveState resolveState, @Nullable PsiElement psiElement, @NotNull PsiElement psiElement1) {
        return false;
    }

    @Nullable
    @Override
    public PsiElement getContext() {
        return null;
    }

    @Override
    public boolean isPhysical() {
        return false;
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return null;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return null;
    }

    @Override
    public ASTNode getNode() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(PsiElement psiElement) {
        return false;
    }

    @Override
    public Icon getIcon(int i) {
        return null;
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {

    }
}
