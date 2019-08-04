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

package net.werpu.tools.toolWindows.supportive;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.IconLoader;
import net.werpu.tools.supportive.fs.common.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;

/**
 * separate node renderer class defining
 * our contexts in the various trees
 */
public class ContextNodeRenderer extends NodeRenderer {

    private final Icon ng = IconLoader.getIcon("/images/ng.png");

    @Nullable
    @Override
    protected ItemPresentation getPresentation(Object node) {
        ItemPresentation retVal = super.getPresentation(node);
        Icon icon = ng;
        if (node instanceof IAngularFileContext) {
            icon = ((IAngularFileContext) node).getIcon();
        }
        if (node instanceof PsiRouteContext) {
            Route route = ((PsiRouteContext) node).getRoute();
            return new PresentationData(route.getRouteKey(), route.getUrl(), icon, null);
        } else if (node instanceof NgModuleFileContext) {
            NgModuleFileContext data = (NgModuleFileContext) node;
            return new PresentationData(data.getModuleName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof ComponentFileContext) {
            ComponentFileContext data = (ComponentFileContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof FilterPipeContext) {
            FilterPipeContext data = (FilterPipeContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof ServiceContext) {
            ServiceContext data = (ServiceContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof DtoContext) {
            DtoContext data = (DtoContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        }
        return retVal;
    }
}
