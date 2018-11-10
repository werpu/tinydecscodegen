package providers;

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
