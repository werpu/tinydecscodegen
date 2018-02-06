package supportive.refactor;

public interface IRefactorUnit {
    int getStartOffset();

    int getEndOffset();

    com.intellij.psi.PsiFile getFile();

    String getRefactoredText();
}
