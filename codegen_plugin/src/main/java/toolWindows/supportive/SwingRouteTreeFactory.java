package toolWindows.supportive;


import com.google.common.base.Strings;
import supportive.fs.common.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static actions_all.shared.Labels.*;

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
