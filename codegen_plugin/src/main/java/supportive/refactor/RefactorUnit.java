package supportive.refactor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;

@Getter
public class RefactorUnit implements IRefactorUnit {

    PsiFile file;
    PsiElement psiElement;
    String refactoredText;

    public RefactorUnit(PsiFile file, PsiElement psiElement, String refactoredText) {
        this.file = file;
        this.psiElement = psiElement;
        this.refactoredText = refactoredText;
    }

    @Override
    public int getStartOffset() {
        return psiElement.getTextRange().getStartOffset();
    }

    @Override
    public int getEndOffset() {
        return psiElement.getTextRange().getEndOffset();
    }


}
