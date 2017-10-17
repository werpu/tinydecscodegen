package utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class RefactorUnit {

    PsiFile file;
    PsiElement psiElement;
    String refactoredText;

    public RefactorUnit(PsiFile file, PsiElement psiElement, String refactoredText) {
        this.file = file;
        this.psiElement = psiElement;
        this.refactoredText = refactoredText;
    }

    public PsiFile getFile() {
        return file;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public String getRefactoredText() {
        return refactoredText;
    }

    public int getStartOffset() {
        return psiElement.getTextRange().getStartOffset();
    }

    public int getEndOffset() {
        return psiElement.getTextRange().getEndOffset();
    }


}
