package toolWindows;

import com.google.common.base.Strings;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.*;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static supportive.utils.StringUtils.elVis;


public class AngularStructureToolWindow implements ToolWindowFactory {

    private Tree tree = new Tree();

    private gui.AngularStructureToolWindow contentPanel = new gui.AngularStructureToolWindow();

    private IntellijFileContext projectRoot = null;
    private TreeSpeedSearch searchPath = null;
    private TreeExpansionMonitor expansionMonitor = null;

    public AngularStructureToolWindow() {

        final Icon ng = IconLoader.getIcon("/images/ng.png");

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


        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Please Wait")));


        tree.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!(tree.getLastSelectedPathComponent() instanceof SwingRouteTreeNode)) {
                    return;
                }
                SwingRouteTreeNode selectedNode = (SwingRouteTreeNode) tree.getLastSelectedPathComponent();
                if (e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (selectedNode.getUserObject() instanceof PsiRouteContext) {
                        PsiRouteContext foundContext = (PsiRouteContext) selectedNode.getUserObject();
                        goToComponent(foundContext);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (selectedNode.getUserObject() instanceof PsiRouteContext) {
                        PsiRouteContext foundContext = (PsiRouteContext) selectedNode.getUserObject();
                        goToRouteDcl(foundContext);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

    }


    private void doMouseClicked(MouseEvent ev) {
        TreePath tp = tree.getPathForLocation(ev.getX(), ev.getY());
        if (tp == null) {
            return;
        }
        Object selectedNode = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
        if (selectedNode == null) {
            return;
        }

        if (selectedNode instanceof PsiRouteContext) {
            PsiRouteContext foundContext = (PsiRouteContext) selectedNode;


            if (SwingUtilities.isRightMouseButton(ev)) {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem go_to_route_declaration = new JMenuItem("Go to route declaration");

                go_to_route_declaration.addActionListener(actionEvent -> goToRouteDcl(foundContext));
                popupMenu.add(go_to_route_declaration);

                JMenuItem go_to_component = new JMenuItem("Go to component");
                go_to_component.addActionListener(actionEvent -> goToComponent(foundContext));
                popupMenu.add(go_to_component);

                popupMenu.addSeparator();
                JMenuItem copy_route_key = new JMenuItem("Copy Route Key");
                copy_route_key.addActionListener(actionEvent -> copyRouteName(foundContext));
                popupMenu.add(copy_route_key);

                JMenuItem copy_route_link = new JMenuItem("Copy Route Link");
                copy_route_link.addActionListener(actionEvent -> copyRouteLink(foundContext));
                popupMenu.add(copy_route_link);

                popupMenu.show(tree, ev.getX(), ev.getY());
            }
        }
    }


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                //TODO angular version dynamic depending on the project type
                if (!file.getName().endsWith(".ts")) {
                    return;
                }
                DumbService.getInstance(projectRoot.getProject()).smartInvokeLater(() -> {
                    //List<IntellijFileContext> angularRoots = AngularIndex.getAllAngularRoots(project, NG);
                    final IntellijFileContext vFileContext;
                    try {
                        vFileContext = new IntellijFileContext(project, file);
                    } catch(RuntimeException ex) {
                        //TODO logging here, the project was not resolvable
                        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Project structure cannot be determined atm. Please try again later.")));
                        return;
                    }

                    boolean routeFileAffected = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                            .anyMatch(routeFile -> routeFile.equals(vFileContext));

                    if (routeFileAffected) {
                        refreshContent(projectRoot.getProject());
                    }
                });
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        });

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, true);

        refreshContent(project);
        panel.setContent(contentPanel.getMainPanel());
        panel.setBackground(UIUtil.getFieldForegroundColor());

        contentPanel.getScollPanel().setViewportView(tree);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);


        if (toolWindow instanceof ToolWindowEx) {
            AnAction[] titleActions = new AnAction[]{
                    CommonActionsManager.getInstance().createExpandAllHeaderAction(tree),
                    CommonActionsManager.getInstance().createCollapseAllHeaderAction(tree)
            };
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);
        }


    }

    private void refreshContent(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                try {
                    projectRoot = new IntellijFileContext(project);
                } catch(RuntimeException ex) {
                    //TODO logging here, the project was not resolvable
                    tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Project structure cannot be determined atm. Please try again later.")));
                    return;
                }


                List<IUIRoutesRoutesFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot);
                if (routeFiles == null || routeFiles.isEmpty()) {
                    tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No route found")));
                    return;
                }

                SwingRootParentNode rootNode = new SwingRootParentNode("Routes");
                DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
                routeFiles.forEach(ctx -> {

                            String node = ctx instanceof NG_UIRoutesRoutesFileContext ? "Angluar NG  Routes" :
                                    ctx instanceof TNAngularRoutesFileContext ? "TN Dec Routes" :
                                            "TN Dec UI Routes";
                            DefaultMutableTreeNode routes = SwingRouteTreeFactory.createRouteTrees(ctx, node);
                            rootNode.add(routes);
                        });
                tree.setRootVisible(false);
                tree.setModel(newModel);

                //now we restore the expansion state

                if (expansionMonitor != null) {
                    expansionMonitor.restore();
                }


                /*found this usefule helper in the jetbrains intellij sources*/
                if (searchPath == null) {

                    expansionMonitor = TreeExpansionMonitor.install(tree);
                    tree.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent me) {
                            doMouseClicked(me);
                        }
                    });

                    searchPath = new TreeSpeedSearch(tree, treePath -> {
                        treePath.getPath();
                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                        final Object userObject = node.getUserObject();
                        TreePath nodePath = new TreePath(node.getPath());
                        if (!tree.isExpanded(nodePath)) {
                            tree.expandPath(nodePath);
                        }

                        if (userObject instanceof PsiRouteContext) {
                            return ((PsiRouteContext) userObject).getRoute().getRouteVarName();
                        }
                        return null;

                    });
                }

            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }


    private void goToRouteDcl(PsiRouteContext foundContext) {
        openEditor(foundContext);
    }

    private void openEditor(PsiRouteContext foundContext) {
        FileEditor[] editors = (FileEditorManager.getInstance(foundContext.getElement().getProject())).openFile(foundContext.getElement().getContainingFile().getVirtualFile(), true);
        if (editors.length > 0 && elVis(editors[0], "editor").isPresent()) {
            CaretModel editor = ((Editor) elVis(editors[0], "editor").get()).getCaretModel();
            editor.moveToOffset(foundContext.getTextOffset());
            editor.moveCaretRelatively(0, 0, false, false, true);
        }
    }

    private void goToComponent(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (Strings.isNullOrEmpty(route.getComponentPath())) {
            Messages.showErrorDialog(this.tree.getRootPane(), "No component determinable - please check your route declaration.", actions_all.shared.Messages.ERR_OCCURRED);
            return;
        }
        Path componentPath = Paths.get(route.getComponentPath());
        Path parent = Paths.get(Objects.requireNonNull(foundContext.getElement().getContainingFile().getParent()).getVirtualFile().getPath());
        Path rel = parent.relativize(componentPath);
        VirtualFile virtualFile = foundContext.getElement().getContainingFile().getParent().getVirtualFile().findFileByRelativePath(rel.toString().replaceAll("\\\\", "/") + ".ts");
        if(virtualFile != null) {
            (FileEditorManager.getInstance(foundContext.getElement().getProject())).openFile(virtualFile, true);
        }

    }

    /**
     * copies the route name into the clipboard
     */
    private void copyRouteName(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (route == null) {
            return;
        }
        StringSelection stringSelection = new StringSelection(route.getRouteKey());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * copies the fully qualified route link into the clipboard
     * to be inserted somewhere in the code
     */
    private void copyRouteLink(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (route == null) {
            return;
        }
        StringSelection stringSelection;
        if (route.getOriginContext().equals(TNUIRoutesFileContext.class)) {
            stringSelection = new StringSelection("<a ui-sref=\"" + route.getRouteKey() + "\" ui-sref-active=\"active\">" + route.getRouteVarName() + "</a>");
        } else if (route.getOriginContext().equals(TNAngularRoutesFileContext.class)) {
            stringSelection = new StringSelection("<a href=\"#" + route.getUrl() + "\">" + route.getRouteVarName() + "</a>");
        } else {
            stringSelection = new StringSelection("<a uiSref=\"" + route.getRouteKey() + "\" uiSrefActive=\"active\">" + route.getRouteVarName() + "</a>");
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }


}
