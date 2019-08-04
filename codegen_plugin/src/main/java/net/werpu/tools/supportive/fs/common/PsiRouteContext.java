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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.swing.*;


@EqualsAndHashCode(callSuper=false)
public class PsiRouteContext extends PsiElementContext implements IAngularFileContext {

    @Getter
    private final Route route;

    public PsiRouteContext(PsiElement element, Route route) {
        super(element);
        this.route = route;
    }

    @Override
    public String getDisplayName() {
        return route.getRouteKey();
    }

    @Override
    public String getResourceName() {
        return route.getRouteKey();
    }

    @Override
    public NgModuleFileContext getParentModule() {
        throw new RuntimeException("Not supported in this class");
    }

    @Override
    public VirtualFile getVirtualFile() {
        return getPsiFile().getVirtualFile();
    }

    @Override
    public PsiFile getPsiFile() {
        return element.getContainingFile();
    }

    public Icon getIcon() {
        return AllIcons.Nodes.Jsf.NavigationCase;
    }
}
