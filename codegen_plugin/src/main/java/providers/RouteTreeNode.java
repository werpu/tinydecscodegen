package providers;

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.PsiRouteContext;

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
        RouteTreeNode treeNode = new RouteTreeNode(getProject(), value);
        _routeIdx.put(value.getName(), treeNode);
    }

    public void addSubRoute(PsiRouteContext psiRouteContext) {
        String parentRoute = psiRouteContext.getName();
        if (parentRoute.contains(".")) {
            parentRoute = parentRoute.substring(0, parentRoute.lastIndexOf("."));
        }
        if (_routeIdx.containsKey(parentRoute)) {
            RouteTreeNode treeNode = new RouteTreeNode(getProject(), psiRouteContext);
            _routeIdx.get(parentRoute).getChildren().add(treeNode);
            _routeIdx.put(psiRouteContext.getName(), treeNode);
        }

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
