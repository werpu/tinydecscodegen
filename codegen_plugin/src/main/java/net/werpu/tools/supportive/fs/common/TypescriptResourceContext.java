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

package net.werpu.tools.supportive.fs.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;

public class TypescriptResourceContext extends TypescriptFileContext {
    @Getter
    protected PsiElementContext resourceRoot;

    public TypescriptResourceContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public TypescriptResourceContext(AnActionEvent event) {
        super(event);
    }

    public TypescriptResourceContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public TypescriptResourceContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    public TypescriptResourceContext(Project project, PsiFile psiFile, PsiElement psiElement) {
        super(project, psiFile);
        this.resourceRoot = new PsiElementContext(psiElement);
    }

    public TypescriptResourceContext(AnActionEvent event, PsiElement psiElement) {
        super(event);
        this.resourceRoot = new PsiElementContext(psiElement);
    }

    public TypescriptResourceContext(Project project, VirtualFile virtualFile, PsiElement psiElement) {
        super(project, virtualFile);
        this.resourceRoot = new PsiElementContext(psiElement);
    }

    public TypescriptResourceContext(IntellijFileContext fileContext, PsiElement psiElement) {
        super(fileContext);
        this.resourceRoot = new PsiElementContext(psiElement);
    }
}
