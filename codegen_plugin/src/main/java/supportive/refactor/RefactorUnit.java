package supportive.refactor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import supportive.fs.common.PsiElementContext;

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

    public RefactorUnit(PsiFile file, PsiElementContext psiElement, String refactoredText) {
        this.file = file;
        this.psiElement = psiElement.getElement();
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
