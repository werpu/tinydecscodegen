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

package net.werpu.tools.providers;

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.PsiRouteContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract treenode for our Psi Route Contexts
 */
@Setter
@Getter
public class RouteTreeNode extends AbstractTreeNode<PsiRouteContext> {
    List<RouteTreeNode> children = Lists.newLinkedList();
    Map<String, RouteTreeNode> _routeIdx = new HashMap<>();

    public RouteTreeNode(Project project, PsiRouteContext value) {
        super(project, value);

        _routeIdx.put(value.getName(), this);
        NodeDescriptor<PsiRouteContext> nodeDescriptor = new RouteNodeDescriptor(project, value);
        applyFrom(nodeDescriptor);

    }

    //TODO check how to handle go to component
    @Override
    protected VirtualFile getVirtualFile() {
        return getValue().getElement().getContainingFile().getVirtualFile();
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @NotNull
    @Override
    public Collection<RouteTreeNode> getChildren() {
        return children;
    }

    @Override
    protected void update(PresentationData presentationData) {

    }

}
