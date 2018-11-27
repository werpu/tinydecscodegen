package net.werpu.tools.supportive.refactor;

/**
 * generic interface describing a refactoring
 * a refactoring is always an atomic substitution on the AST
 *
 * aka following the crud rules
 *
 * insert -> replace "" -> with someting
 * update -> remove and insert with something differet
 * remove  something -> ""
 *
 * A more complex refactoring is a chain of refactor units
 * ATM we only allow refactorings on a single file
 * (multi file refactorings are atm not part of any plugin
 * functionality)
 */
public interface IRefactorUnit {

    /**
     * @return absolute start offset (to the parent document root)
     */
    int getStartOffset();

    /**
     * @return absolute end offset (to the parent document root)
     */
    int getEndOffset();

    /**
     * @return the parent document file referencing this refactoring
     * might be needed in the future for multi document refactorings
     */
    com.intellij.psi.PsiFile getFile();

    /**
     * @return the execution result of the refactoring without any parent
     * document writeback (the caller needs to take care of writing back)
     */
    String getRefactoredText();
}
