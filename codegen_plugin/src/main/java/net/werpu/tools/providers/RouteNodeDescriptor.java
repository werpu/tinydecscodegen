package net.werpu.tools.providers;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import net.werpu.tools.supportive.fs.common.PsiRouteContext;

public class RouteNodeDescriptor extends NodeDescriptor<PsiRouteContext> {

    PsiRouteContext element;

    public RouteNodeDescriptor(@Nullable Project project, PsiRouteContext element) {
        super(project, null);
        this.element = element;
        myName = element.getRoute().getRouteKey() + " [" + element.getRoute().getUrl() + "]";
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public PsiRouteContext getElement() {
        return element;
    }
}
