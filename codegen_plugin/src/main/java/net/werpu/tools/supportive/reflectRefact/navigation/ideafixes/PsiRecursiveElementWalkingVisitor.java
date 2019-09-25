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

import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a PSI element visitor which recursively visits the children of the element
 * on which the visit was started.
 * <p>
 * Sidenote I had to crossport this
 * class because of a gigantic f***up
 * from the idea guys who blocked an api
 * without documenting any replacment
 */
public abstract class PsiRecursiveElementWalkingVisitor extends PsiElementVisitor implements PsiRecursiveVisitor {
    private final boolean myVisitAllFileRoots;
    private final PsiWalkingState myWalkingState = new PsiWalkingState(this) {
        @Override
        public void elementFinished(@NotNull PsiElement element) {
            PsiRecursiveElementWalkingVisitor.this.elementFinished(element);
        }
    };

    protected PsiRecursiveElementWalkingVisitor() {
        this(false);
    }

    protected PsiRecursiveElementWalkingVisitor(boolean visitAllFileRoots) {
        myVisitAllFileRoots = visitAllFileRoots;
    }

    @Override
    public void visitElement(final PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();

        myWalkingState.elementStarted(element);
    }

    protected void elementFinished(PsiElement element) {

    }

    @Override
    public void visitFile(final PsiFile file) {
        if (myVisitAllFileRoots) {
            final FileViewProvider viewProvider = file.getViewProvider();
            final List<PsiFile> allFiles = viewProvider.getAllFiles();
            if (allFiles.size() > 1) {
                if (file == viewProvider.getPsi(viewProvider.getBaseLanguage())) {
                    for (PsiFile lFile : allFiles) {
                        lFile.acceptChildren(this);
                    }
                    return;
                }
            }
        }

        super.visitFile(file);
    }

    public void stopWalking() {
        myWalkingState.stopWalking();
    }
}
