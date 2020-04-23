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

import com.google.common.base.Strings;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import lombok.Getter;
import net.werpu.tools.supportive.fs.common.PsiRouteContext;
import net.werpu.tools.supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a tree structure provider which displays
 * the navigational structure tree
 */
public class NavTreeStructureProvider implements TreeStructureProvider {
    @Getter
    NG_UIRoutesRoutesFileContext ctx = null;
    @Getter
    List<RouteTreeNode> treeNodes = new LinkedList<>();

    public NavTreeStructureProvider(NG_UIRoutesRoutesFileContext ctx) {
        this.ctx = ctx;

        makeRouteTreeNodes(ctx);
    }

    public void makeRouteTreeNodes(NG_UIRoutesRoutesFileContext ctx) {
        Map<String, RouteTreeNode> _routeIdx = new HashMap<>();

        //lets make the tree Nodes
        List<PsiRouteContext> sortedRoutes = ctx.getRoutes().stream()
                .sorted((PsiRouteContext o1, PsiRouteContext o2) -> o1.getRoute().compareTo(o2.getRoute()))
                .collect(Collectors.toList());

        for (PsiRouteContext route : sortedRoutes) {

            String routeKey = route.getRoute().getRouteKey();
            String subKey = routeKey.contains(".") ? routeKey.substring(0, routeKey.lastIndexOf(".")) : "";
            if (_routeIdx.containsKey(routeKey)) {
                //route already processed
                continue;
            }
            RouteTreeNode newNode = new RouteTreeNode(ctx.getProject(), route);
            if (!Strings.isNullOrEmpty(subKey) && _routeIdx.containsKey(subKey)) {
                _routeIdx.get(subKey).getChildren().add(newNode);
                _routeIdx.put(routeKey, newNode);
                continue;
            }

            treeNodes.add(newNode);
            _routeIdx.put(routeKey, newNode);

        }
    }

    @NotNull
    @Override
    public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        return null;
    }

    @Nullable
    @Override
    public Object getData(@NotNull Collection<AbstractTreeNode<?>> selected, @NotNull String dataId) {
        return null;
    }
}
