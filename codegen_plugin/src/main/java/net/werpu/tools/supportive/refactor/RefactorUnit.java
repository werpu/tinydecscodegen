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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.PsiElementContext;

/**
 * Refactoring replacement unit
 *
 * The idea is that a refactoring of text
 * usually is either a replace or insert, but insert
 * is a special kind of replace where an existing position
 * with zero length is replaced by a text.
 */
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
