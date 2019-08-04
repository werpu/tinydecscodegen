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

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.Getter;

import java.util.List;

import static net.werpu.tools.supportive.reflectRefact.IntellijRefactor.NG_MODULE;

public abstract class AngularResourceContext extends TypescriptResourceContext implements IAngularFileContext {
    @Getter
    protected String clazzName;
    @Getter
    protected NgModuleFileContext parentModule;
    @Getter
    protected String artifactName;

    public AngularResourceContext(Project project, PsiFile psiFile) {
        super(project, psiFile);
    }

    public AngularResourceContext(AnActionEvent event) {
        super(event);
    }

    public AngularResourceContext(Project project, VirtualFile virtualFile) {
        super(project, virtualFile);
    }

    public AngularResourceContext(IntellijFileContext fileContext) {
        super(fileContext);
    }

    public AngularResourceContext(Project project, PsiFile psiFile, PsiElement psiElement) {
        super(project, psiFile, psiElement);
    }

    public AngularResourceContext(AnActionEvent event, PsiElement psiElement) {
        super(event, psiElement);
    }

    public AngularResourceContext(Project project, VirtualFile virtualFile, PsiElement psiElement) {
        super(project, virtualFile, psiElement);
    }

    public AngularResourceContext(IntellijFileContext fileContext, PsiElement psiElement) {
        super(fileContext, psiElement);
    }


    protected void findParentModule() {
        List<IntellijFileContext> modules = findFirstUpwards(psiFile -> psiFile.getContainingFile().getText().contains(NG_MODULE));
        this.parentModule = modules.isEmpty() ? null :  new NgModuleFileContext(modules.get(0));
    }

    public String getDisplayName() {
        String finalArtifactName = Strings.nullToEmpty(artifactName);
        String finalModuleName = parentModule != null ? parentModule.getModuleName() : "";
        return finalArtifactName+" ["+finalModuleName+"]";
    }

    


}
