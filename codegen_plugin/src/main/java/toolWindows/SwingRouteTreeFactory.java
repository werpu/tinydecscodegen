package toolWindows;

import com.jgoodies.common.base.Strings;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.IUIRoutesRoutesFileContext;
import supportive.fs.common.NgModuleFileContext;
import supportive.fs.common.PsiRouteContext;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwingRouteTreeFactory {




    public static DefaultMutableTreeNode createRouteTrees(IUIRoutesRoutesFileContext ctx, String label) {
        Map<String, SwingRouteTreeNode> _routeIdx = new HashMap<>();
        DefaultMutableTreeNode treeNodes = new SwingRootParentNode(label);

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


    public static DefaultMutableTreeNode createModulesTree(List<NgModuleFileContext> ctx, String label) {
        Map<String, SwingRouteTreeNode> _routeIdx = new HashMap<>();
        DefaultMutableTreeNode treeNodes = new SwingRootParentNode(label);

        //lets make the tree Nodes
        List<NgModuleFileContext> sortedRoutes =  ctx.stream().sorted(Comparator.comparing(NgModuleFileContext::getModuleName)).collect(Collectors.toList());

        for (NgModuleFileContext module : sortedRoutes) {

            String moduleName = module.getModuleName();
            /*String subKey = moduleName.contains(".") ? moduleName.substring(0,moduleName.lastIndexOf(".")) : "";
            if(_routeIdx.containsKey(moduleName)) {
                //route already processed
                continue;
            }*/
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(module);
            /*if(!Strings.isBlank(subKey) && _routeIdx.containsKey(subKey)) {
                _routeIdx.get(subKey).add(newNode);
                _routeIdx.put(moduleName, newNode);
                continue;
            }*/

            treeNodes.add(newNode);
            //_routeIdx.put(moduleName, newNode);

        }
        return treeNodes;
    }

    public static DefaultMutableTreeNode createComponentsTree(List<ComponentFileContext> ctx, String label) {
        Map<String, SwingRouteTreeNode> _routeIdx = new HashMap<>();
        DefaultMutableTreeNode treeNodes = new SwingRootParentNode(label);

        //lets make the tree Nodes
        List<ComponentFileContext> contexts =  ctx.stream().sorted(Comparator.comparing(ComponentFileContext::getDisplayName)).collect(Collectors.toList());

        for (ComponentFileContext context : contexts) {

            String displayName = context.getDisplayName();
            /*String subKey = displayName.contains(".") ? displayName.substring(0,displayName.lastIndexOf(".")) : "";
            if(_routeIdx.containsKey(displayName)) {
                //route already processed
                continue;
            }*/
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(context);
            /*if(!Strings.isBlank(subKey) && _routeIdx.containsKey(subKey)) {
                _routeIdx.get(subKey).add(newNode);
                _routeIdx.put(displayName, newNode);
                continue;
            }*/

            treeNodes.add(newNode);
            //_routeIdx.put(displayName, newNode);

        }
        return treeNodes;
    }


}
