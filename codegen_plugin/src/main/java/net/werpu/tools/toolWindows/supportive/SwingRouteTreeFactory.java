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


import com.google.common.base.Strings;
import net.werpu.tools.supportive.fs.common.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.werpu.tools.actions_all.shared.Labels.*;

public class SwingRouteTreeFactory {

    /**
     * creates a rout tree node from a given route file context
     */
    public static DefaultMutableTreeNode createRouteTrees(IUIRoutesRoutesFileContext ctx, String label) {
        Map<String, SwingRouteTreeNode> _routeIdx = new HashMap<>();
        DefaultMutableTreeNode treeNodes = new SwingRootParentNode(label);

        //lets make the tree Nodes
        List<PsiRouteContext> sortedRoutes = (List<PsiRouteContext>) ctx.getRoutes().stream().sorted(Comparator.comparing(PsiRouteContext::getRoute)).collect(Collectors.toList());

        for (PsiRouteContext route : sortedRoutes) {

            String routeKey = route.getRoute().getRouteKey();
            String subKey = routeKey.contains(".") ? routeKey.substring(0, routeKey.lastIndexOf(".")) : "";
            if (_routeIdx.containsKey(routeKey)) {
                //route already processed
                continue;
            }
            SwingRouteTreeNode newNode = new SwingRouteTreeNode(route);
            if (!Strings.isNullOrEmpty(subKey) && _routeIdx.containsKey(subKey)) {
                _routeIdx.get(subKey).add(newNode);
                _routeIdx.put(routeKey, newNode);
                continue;
            }

            treeNodes.add(newNode);
            _routeIdx.put(routeKey, newNode);

        }
        return treeNodes;
    }


    public static DefaultMutableTreeNode createModulesTree(List<NgModuleFileContext> ctx, String label) {
        return createFlatTreeTree(ctx, label, (item) -> item.getModuleName());
    }

    public static DefaultMutableTreeNode createComponentsTree(List<ComponentFileContext> ctx, String label) {
        return createFlatTreeTree(ctx, label, (item) -> item.getDisplayName());
    }


    public static <T, R extends String> DefaultMutableTreeNode createFlatTreeTree(List<T> itemsTn, String label, Function<T, R> keyExtractor) {

        DefaultMutableTreeNode treeNodes = new SwingRootParentNode(label);
        List<T> contexts = itemsTn.stream().sorted(Comparator.comparing(keyExtractor)).collect(Collectors.toList());

        for (T context : contexts) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(context);
            treeNodes.add(newNode);
        }
        return treeNodes;
    }

    public static DefaultMutableTreeNode createResourcesTree(ResourceFilesContext itemsTn, String label) {
        DefaultMutableTreeNode servicesTree = createFlatTreeTree(itemsTn.getServices(), LBL_SERVICES, (item) -> item.getDisplayName());
        DefaultMutableTreeNode controllers = createFlatTreeTree(itemsTn.getControllers(), LBL_CONTROLLERS, (item) -> item.getDisplayName());
        DefaultMutableTreeNode compoonentsTree = createComponentsTree(itemsTn.getComponents(), LBL_COMPONENTS);
        DefaultMutableTreeNode filtersTree = createFlatTreeTree(itemsTn.getFiltersPipes(), LBL_FILTERS, (item) -> item.getDisplayName());

        DefaultMutableTreeNode treeNodes = new SwingRootParentNode(label);
        treeNodes.add(servicesTree);
        treeNodes.add(controllers);
        treeNodes.add(compoonentsTree);
        treeNodes.add(filtersTree);
        return treeNodes;
    }
}
