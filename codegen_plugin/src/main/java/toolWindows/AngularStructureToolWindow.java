package toolWindows;

import com.google.common.base.Strings;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import indexes.TN_UIRoutesIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.*;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static supportive.reflectRefact.PsiWalkFunctions.JS_ES_6_FROM_CLAUSE;
import static supportive.reflectRefact.PsiWalkFunctions.PSI_ELEMENT_JS_STRING_LITERAL;
import static supportive.utils.StringUtils.elVis;
import static supportive.utils.StringUtils.stripQuotes;


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
            //Optional<NG_UIRoutesRoutesFileContext> ctx = (Optional<NG_UIRoutesRoutesFileContext) ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
            //        .filter(item -> item instanceof NG_UIRoutesRoutesFileContext).findFirst();

            //tree.setModel(new DefaultTreeModel(SwingRouteTreeFactory.createRouteTrees(ctx)));
            refreshContent(projectRoot.getProject());
        });

        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Please Wait")));
    }

    public void doMouseClicked(MouseEvent ev) {
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
            System.out.println("Debug");


            if (SwingUtilities.isRightMouseButton(ev)) {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem go_to_route_declaration = new JMenuItem("Go to route declaration");

                go_to_route_declaration.addActionListener(actionEvent -> goToRouteDcl(foundContext));
                popupMenu.add(go_to_route_declaration);

                JMenuItem go_to_component = new JMenuItem("Go to component");
                go_to_component.addActionListener(actionEvent -> {
                    goToComponent(foundContext);
                });
                popupMenu.add(go_to_component);

                popupMenu.addSeparator();
                JMenuItem copy_route_key = new JMenuItem("Copy Route Key");
                copy_route_key.addActionListener(actionEvent -> {
                    copyRouteName(foundContext);
                });
                popupMenu.add(copy_route_key);

                JMenuItem copy_route_link = new JMenuItem("Copy Route Link");
                copy_route_link.addActionListener(actionEvent -> {
                    copyRouteLink(foundContext);
                });
                popupMenu.add(copy_route_link);

                popupMenu.show(tree, ev.getX(), ev.getY());
            }
        }
    }

    public void goToRouteDcl(PsiRouteContext foundContext) {
        openEditor(foundContext);
    }

    public void openEditor(PsiRouteContext foundContext) {
        FileEditor[] editors = (FileEditorManager.getInstance(foundContext.getElement().getProject())).openFile(foundContext.getElement().getContainingFile().getVirtualFile(), true);
        if (editors.length > 0 && elVis(editors[0], "editor").isPresent()) {
            CaretModel editor = ((Editor) elVis(editors[0], "editor").get()).getCaretModel();
            editor.moveToOffset(foundContext.getTextOffset());
            editor.moveCaretRelatively(0, 0, false, false, true);
        }
    }

    public void goToComponent(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        PsiElementContext topCtx = new PsiElementContext(foundContext.getElement().getContainingFile());
        if(Strings.isNullOrEmpty(route.getComponentPath())) {
            Messages.showErrorDialog(this.tree.getRootPane(), "No component determinable - please check your route declaration.", actions_all.shared.Messages.ERR_OCCURRED);
            return;
        }
        Path componentPath = Paths.get(route.getComponentPath());
        Path parent = Paths.get(foundContext.getElement().getContainingFile().getParent().getVirtualFile().getPath());
        Path rel = parent.relativize(componentPath);
        VirtualFile virtualFile = foundContext.getElement().getContainingFile().getParent().getVirtualFile().findFileByRelativePath(rel.toString()+".ts");
        (FileEditorManager.getInstance(foundContext.getElement().getProject())).openFile(virtualFile, true);

    }

    /**
     * copies the route name into the clipboard
     */
    public void copyRouteName(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if(route == null) {
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
    public void copyRouteLink(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if(route == null) {
            return;
        }
        StringSelection stringSelection = null;
        if(route.getOriginContext().equals(TNUIRoutesFileContext.class)) {
            stringSelection = new StringSelection("<a ui-sref=\"" + route.getRouteKey() + "\" ui-sref-active=\"active\">" + route.getRouteVarName() + "</a>");
        } else if (route.getOriginContext().equals(TNAngularRoutesFileContext.class)) {
            stringSelection = new StringSelection("<a href=\"#"+route.getUrl()+"\">" +route.getRouteVarName()+ "</a>");
        } else {
            stringSelection = new StringSelection("<a uiSref=\""+route.getRouteKey()+"\" uiSrefActive=\"active\">"+route.getRouteVarName()+"</a>");
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public void goToNavDeclaration(PsiRouteContext ctx) {

    }

    public void goToComponentDeclaration(PsiRouteContext ctx) {

    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                //TODO angular version dynamic depending on the project type
                if(!file.getName().endsWith(".ts")) {
                    return;
                }
                DumbService.getInstance(projectRoot.getProject()).smartInvokeLater(() -> {
                    //List<IntellijFileContext> angularRoots = AngularIndex.getAllAngularRoots(project, NG);
                    IntellijFileContext vFileContext = new IntellijFileContext(project, file);

                    boolean routeFileAffected = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                            .filter(routeFile -> routeFile.equals(vFileContext)).findFirst().isPresent();

                    if (routeFileAffected) {
                        refreshContent(projectRoot.getProject());
                    }
                });
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        });

        myToolWindow = toolWindow;
        refreshContent(project);
        contentPanel.getScollPanel().setViewportView(tree);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contentPanel.getMainPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void refreshContent(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {

                projectRoot = new IntellijFileContext(project);


                List<IUIRoutesRoutesFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot);
                if(routeFiles == null || routeFiles.isEmpty()) {
                    tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No route found")));
                    return;
                }

                DefaultTreeModel oldModel = (DefaultTreeModel) tree.getModel();
                Set<String> openState = fetchOpenState(oldModel);

                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Routes");

                DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
                routeFiles.stream()
                        .forEach(ctx -> {

                        String node = ctx instanceof NG_UIRoutesRoutesFileContext ? "Angluar NG  Routes" :
                                           ctx instanceof TNAngularRoutesFileContext ?  "TN Dec Routes":
                                                   "TN Dec UI Routes";
                            DefaultMutableTreeNode routes = SwingRouteTreeFactory.createRouteTrees(ctx, node);
                        rootNode.add(routes);
                });
                tree.setRootVisible(false);
                tree.setModel(newModel);

                //now we restore the expansion state
                restoreOpenState(openState, (TreeNode) newModel.getRoot());

                tree.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        doMouseClicked(me);
                    }
                });
            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }

    public void restoreOpenState(Set<String> openIdx, TreeNode newModel) {
        walkTree(newModel, node -> {
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if(userObject instanceof PsiRouteContext) {
                String routeKey = ((PsiRouteContext) userObject).getRoute().getRouteKey();
                if (openIdx.contains(routeKey)) {
                    tree.expandPath(new TreePath(((DefaultMutableTreeNode) node).getPath()));
                }
            }


            return true;
        });
    }

    @NotNull
    public Set<String> fetchOpenState(DefaultTreeModel oldModel) {
        Set<String> openIdx = new HashSet();
        walkTree((TreeNode) oldModel.getRoot(), node -> {
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();

            if(userObject instanceof PsiRouteContext &&
                    tree.isExpanded(new TreePath(((DefaultMutableTreeNode) node).getPath()))) {
                String routeKey = ((PsiRouteContext) userObject).getRoute().getRouteKey();
                openIdx.add(routeKey);
            }
            return true;
        });
        return openIdx;
    }


    boolean walkTree(TreeNode node, Function<TreeNode, Boolean> walkerFunction) {

        boolean ret = walkerFunction.apply(node);
        if(!ret) {
            return false;
        }
        if(node.getChildCount() > 0) {
            for(int cnt = 0; cnt < node.getChildCount(); cnt++) {
                if(!walkTree(node.getChildAt(cnt), walkerFunction )) {
                    return false;
                }
            }
        }
        return true;
    }


}
