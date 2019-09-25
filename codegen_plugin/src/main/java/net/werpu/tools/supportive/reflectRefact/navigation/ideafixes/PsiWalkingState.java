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

package net.werpu.tools.supportive.reflectRefact.navigation.ideafixes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.WalkingState;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;

/**
 * @author cdr
 * <p>
 * Sidenote I had to crossport this
 * class because of a gigantic f***up
 * from the idea guys who blocked an api
 * without documenting any replacment
 */
@CustomLog()
public abstract class PsiWalkingState extends WalkingState<PsiElement> {
    private final PsiElementVisitor myVisitor;

    protected PsiWalkingState(@NotNull PsiElementVisitor delegate) {
        this(delegate, PsiTreeGuide.instance);
    }

    protected PsiWalkingState(@NotNull PsiElementVisitor delegate, @NotNull TreeGuide<PsiElement> guide) {
        super(guide);
        myVisitor = delegate;
    }

    @Override
    public void visit(@NotNull PsiElement element) {
        element.accept(myVisitor);
    }

    @Override
    public void elementStarted(@NotNull PsiElement element) {

        super.elementStarted(element);
    }

    private static class PsiTreeGuide implements TreeGuide<PsiElement> {
        private static final PsiTreeGuide instance = new PsiTreeGuide();

        private static PsiElement checkSanity(PsiElement element, PsiElement sibling) {
            if (sibling == PsiUtilCore.NULL_PSI_ELEMENT)
                throw new PsiInvalidElementAccessException(element, "Sibling of " + element + " is NULL_PSI_ELEMENT");
            return sibling;
        }

        @Override
        public PsiElement getNextSibling(@NotNull PsiElement element) {
            return checkSanity(element, element.getNextSibling());
        }

        @Override
        public PsiElement getPrevSibling(@NotNull PsiElement element) {
            return checkSanity(element, element.getPrevSibling());
        }

        @Override
        public PsiElement getFirstChild(@NotNull PsiElement element) {
            return element.getFirstChild();
        }

        @Override
        public PsiElement getParent(@NotNull PsiElement element) {
            return element.getParent();
        }
    }
}
