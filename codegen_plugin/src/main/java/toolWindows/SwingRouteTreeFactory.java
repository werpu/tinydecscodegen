package toolWindows;

import com.jgoodies.common.base.Strings;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.ng.UIRoutesRoutesFileContext;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.stream.Collectors;

public class SwingRouteTreeFactory {




    public static DefaultMutableTreeNode createRouteTrees(UIRoutesRoutesFileContext ctx) {
        Map<String, SwingRouteTreeNode> _routeIdx = new HashMap<>();
        DefaultMutableTreeNode treeNodes = new DefaultMutableTreeNode("Routes");

        //lets make the tree Nodes
        List<PsiRouteContext> sortedRoutes = (List<PsiRouteContext>) ctx.getRoutes().stream().sorted(Comparator.comparing(PsiRouteContext::getRoute)).collect(Collectors.toList());

        for (PsiRouteContext route : sortedRoutes) {

            String routeKey = route.getRoute().getRouteKey();
            String subKey = routeKey.contains(".") ? routeKey.substring(0,routeKey.lastIndexOf(".")) : "";
            if(_routeIdx.containsKey(routeKey)) {
                //route already processed
                continue;
            }
            SwingRouteTreeNode newNode = new SwingRouteTreeNode(route);
            if(!Strings.isBlank(subKey) && _routeIdx.containsKey(subKey)) {
                _routeIdx.get(subKey).add(newNode);
                _routeIdx.put(routeKey, newNode);
                continue;
            }

            treeNodes.add(newNode);
            _routeIdx.put(routeKey, newNode);

        }
        return treeNodes;
    }

}
