package providers;

import com.google.common.base.Strings;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;

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
        List<PsiRouteContext> sortedRoutes = (List<PsiRouteContext>) ctx.getRoutes().stream().sorted(Comparator.comparing(PsiRouteContext::getRoute)).collect(Collectors.toList());

        PsiRouteContext oldData = null;
        RouteTreeNode oldNode = null;
        for (PsiRouteContext route : sortedRoutes) {

            String routeKey = route.getRoute().getRouteKey();
            String subKey = routeKey.contains(".") ? routeKey.substring(0,routeKey.lastIndexOf(".")) : "";
            if(_routeIdx.containsKey(routeKey)) {
                //route already processed
                continue;
            }
            RouteTreeNode newNode = new RouteTreeNode(ctx.getProject(), route);
            if(!Strings.isNullOrEmpty(subKey) && _routeIdx.containsKey(subKey)) {
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
    public Collection<AbstractTreeNode> modify(@NotNull AbstractTreeNode abstractTreeNode, @NotNull Collection<AbstractTreeNode> collection, ViewSettings viewSettings) {
        //get the navs and subnavs with all the extra data

        //not needed
        return null;
    }

    @Nullable
    @Override
    public Object /*PsiRouteContext*/ getData(Collection<AbstractTreeNode> selected, String dataName) {

        return ((RouteTreeNode) selected.stream().filter(item -> item.getName().equals(dataName)).findFirst().get()).getValue();
    }
}
