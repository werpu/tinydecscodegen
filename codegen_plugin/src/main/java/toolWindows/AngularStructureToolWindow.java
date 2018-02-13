package toolWindows;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.ContextFactory;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.PsiRouteContext;
import supportive.fs.common.Route;
import supportive.fs.ng.UIRoutesRoutesFileContext;
import supportive.utils.SwingUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class AngularStructureToolWindow implements ToolWindowFactory {

    private ToolWindow myToolWindow;
    gui.AngularStructureToolWindow contentPanel = new gui.AngularStructureToolWindow();
    Tree tree = new Tree();
    IntellijFileContext projectRoot = null;

    public AngularStructureToolWindow() {
        final Icon ng = IconLoader.getIcon("/images/ng.png");
        NodeRenderer renderer = (NodeRenderer) tree.getCellRenderer();
        tree.setCellRenderer(new NodeRenderer() {
            @Nullable
            @Override
            protected ItemPresentation getPresentation(Object node) {
                ItemPresentation retVal = super.getPresentation(node);
                if (node instanceof PsiRouteContext) {
                    Route route = ((PsiRouteContext) node).getRoute();
                    return new PresentationData(route.getRouteKey(), route.getUrl(), ng, null);
                }
                return retVal;
            }
        });




        contentPanel.getBtClose().addActionListener(e -> myToolWindow.hide(null));
        contentPanel.getBtRefresh().addActionListener(e -> {
            UIRoutesRoutesFileContext ctx = (UIRoutesRoutesFileContext) ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                    .filter(item -> item instanceof UIRoutesRoutesFileContext).findFirst().get();

            tree.setModel(new DefaultTreeModel(SwingRouteTreeFactory.createRouteTrees(ctx)));
        });

    }

    public void doMouseClicked(MouseEvent ev) {
        TreePath tp = tree.getPathForLocation(ev.getX(), ev.getY());
        Object selectedNode = ((DefaultMutableTreeNode)tp.getLastPathComponent()).getUserObject();

        if(selectedNode instanceof PsiRouteContext) {
           PsiRouteContext foundContext = (PsiRouteContext) selectedNode;
            System.out.println("Debug");
        }
        if(SwingUtilities.isRightMouseButton(ev)) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem go_to_route_declaration = new JMenuItem("Go to route declaration");
            go_to_route_declaration.addActionListener(actionEvent -> {

            });
            popupMenu.add(go_to_route_declaration);
            JMenuItem go_to_component = new JMenuItem("Go to component");
            go_to_component.addActionListener(actionEvent -> {

            });
            popupMenu.add(go_to_component);
            popupMenu.show(tree, ev.getX(), ev.getY());
        }

    }

    public void goToNavDeclaration(PsiRouteContext ctx) {

    }

    public void goToComponentDeclaration(PsiRouteContext ctx) {

    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        myToolWindow = toolWindow;
        refreshContent(project);
        contentPanel.getScollPanel().setViewportView(tree);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contentPanel.getMainPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void refreshContent(@NotNull Project project) {
        projectRoot = new IntellijFileContext(project);
        UIRoutesRoutesFileContext ctx = (UIRoutesRoutesFileContext) ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                .filter(item -> item instanceof UIRoutesRoutesFileContext).findFirst().get();

        tree.setModel(new DefaultTreeModel(SwingRouteTreeFactory.createRouteTrees(ctx)));
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                doMouseClicked(me);
            }
        });
    }
}
