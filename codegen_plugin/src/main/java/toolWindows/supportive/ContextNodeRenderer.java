package toolWindows.supportive;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.*;

import javax.swing.*;

import static supportive.utils.StringUtils.normalizePath;

/**
 * separate node renderer class defining
 * our contexts in the various trees
 */
public class ContextNodeRenderer extends NodeRenderer {

    private final Icon ng = IconLoader.getIcon("/images/ng.png");

    @Nullable
    @Override
    protected ItemPresentation getPresentation(Object node) {
        ItemPresentation retVal = super.getPresentation(node);
        Icon icon = ng;
        if (node instanceof IAngularFileContext) {
            icon = ((IAngularFileContext) node).getIcon();
        }
        if (node instanceof PsiRouteContext) {
            Route route = ((PsiRouteContext) node).getRoute();
            return new PresentationData(route.getRouteKey(), route.getUrl(), icon, null);
        } else if (node instanceof NgModuleFileContext) {
            NgModuleFileContext data = (NgModuleFileContext) node;
            return new PresentationData(data.getModuleName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof ComponentFileContext) {
            ComponentFileContext data = (ComponentFileContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof FilterPipeContext) {
            FilterPipeContext data = (FilterPipeContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof ServiceContext) {
            ServiceContext data = (ServiceContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        } else if (node instanceof DtoContext) {
            DtoContext data = (DtoContext) node;
            return new PresentationData(data.getDisplayName(), normalizePath(data.getVirtualFile().getPath()), icon, null);
        }
        return retVal;
    }
}
