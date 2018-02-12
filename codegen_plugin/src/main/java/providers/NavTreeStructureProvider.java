package providers;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.ng.UIRoutesRoutesFileContext;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * a tree structure provider which displays
 * the navigational structure tree
 */
public class NavTreeStructureProvider implements TreeStructureProvider {

    @Getter
    UIRoutesRoutesFileContext ctx = null;

    @Getter
    List<RouteTreeNode> treeNodes = new LinkedList<>();

    public NavTreeStructureProvider(UIRoutesRoutesFileContext ctx) {
        this.ctx = ctx;

        //lets make the tree Nodes
        List<PsiRouteContext> sortedRoutes = (List<PsiRouteContext>) ctx.getRoutes().stream().sorted(Comparator.comparing(PsiRouteContext::getRoute)).collect(Collectors.toList());

        PsiRouteContext oldData = null;
        RouteTreeNode oldNode = null;
        for (PsiRouteContext route : sortedRoutes) {
            if (oldNode == null || !oldNode.getValue().getName().startsWith(route.getName())) {
                oldNode = new RouteTreeNode(ctx.getProject(), route);
                treeNodes.add(oldNode);
            } else {
                oldNode.addSubRoute(route);
            }
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
