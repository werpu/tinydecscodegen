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
