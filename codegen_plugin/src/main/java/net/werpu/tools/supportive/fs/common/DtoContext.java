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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import javax.swing.*;

import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.DTO_CLASS;
import static net.werpu.tools.supportive.reflectRefact.PsiWalkFunctions.TYPE_SCRIPT_CLASS;

public class DtoContext extends AngularResourceContext {
    public DtoContext(Project project, PsiFile psiFile, PsiElement element) {
        super(project, psiFile, element);
    }

    public DtoContext(AnActionEvent event, PsiElement element) {
        super(event, element);
    }

    public DtoContext(Project project, VirtualFile virtualFile, PsiElement element) {
        super(project, virtualFile, element);
    }

    public DtoContext(IntellijFileContext fileContext, PsiElement element) {
        super(fileContext, element);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();

        resourceRoot = resolveClass();
        clazzName = resolveClass().getName();
        artifactName = clazzName;

        findParentModule();
    }

    private PsiElementContext resolveClass() {
        String name = $q(DTO_CLASS).findFirst().get().getName();
        return $q(TYPE_SCRIPT_CLASS)
                .filter(clz -> name.startsWith(clz.getName()) && name.length() > clz.getName().length())
                .findFirst()
                .get();
    }

    @Override
    public String getResourceName() {
        return getArtifactName();
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Javaee.WebService;
    }

}
