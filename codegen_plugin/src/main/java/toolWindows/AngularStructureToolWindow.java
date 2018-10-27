package toolWindows;

import com.google.common.base.Strings;
import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.psi.PsiFile;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import indexes.AngularIndex;
import indexes.ModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.*;
import supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import supportive.fs.tn.TNAngularRoutesFileContext;
import supportive.fs.tn.TNUIRoutesFileContext;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static actions_all.shared.Labels.*;
import static actions_all.shared.Messages.*;
import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.utils.StringUtils.normalizePath;
import static supportive.utils.SwingUtils.copyToClipboard;
import static supportive.utils.SwingUtils.openEditor;


public class AngularStructureToolWindow implements ToolWindowFactory {

    private Tree tree = new Tree();

    private gui.AngularStructureToolWindow contentPanel = new gui.AngularStructureToolWindow();

    private IntellijFileContext projectRoot = null;
    private TreeSpeedSearch searchPath = null;
    private TreeExpansionMonitor expansionMonitor = null;


    public AngularStructureToolWindow() {

        tree.setCellRenderer(new ContextNodeRenderer());
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(MSG_PLEASE_WAIT)));


        NodeKeyController<PsiRouteContext> keyCtrl = new NodeKeyController<>(tree,
                this::goToComponent, this::goToRouteDcl, this::copyRouteLink);
        tree.addKeyListener(keyCtrl);

    }


    private void showPopup(PsiRouteContext foundContext, MouseEvent ev) {

        PopupBuilder builder = new PopupBuilder();
        builder.withMenuItem(LBL_GO_TO_ROUTE_DECLARATION, actionEvent -> goToRouteDcl(foundContext))
                .withMenuItem(LBL_GO_TO_COMPONENT, actionEvent -> goToComponent(foundContext))
                .withSeparator()
                .withMenuItem(LBL_COPY_ROUTE_LINK, actionEvent -> copyRouteLink(foundContext))
                .withMenuItem(LBL_COPY_ROUTE_KEY, actionEvent -> copyRouteName(foundContext))
                .show(tree, ev.getX(), ev.getY());
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

                //document listener which refreshes every time a route file changes
                getChangeListener().smartInvokeLater(() -> refreshContent(project, file));
                //TODO add the same check for modules which handle all the artifacts
            }

            private DumbService getChangeListener() {
                return DumbService.getInstance(projectRoot.getProject());
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

    private void refreshContent( @NotNull Project project, VirtualFile file) {
        IntellijFileContext vFileContext = new IntellijFileContext(project, file);

        boolean routeFileAffected = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                .anyMatch(routeFile -> routeFile.equals(vFileContext));


        ContextFactory.getInstance(projectRoot).getProjectResources(projectRoot, TN_DEC);

        if (routeFileAffected) {
            refreshContent(projectRoot.getProject());
            return;
        }

        //ModuleIndex allModules = ModuleIndex.getAllAffectedFiles(, )
        routeFileAffected = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                .anyMatch(routeFile -> routeFile.equals(vFileContext));

        if (routeFileAffected) {
            refreshContent(projectRoot.getProject());
        }
    }

    private void refreshContent(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                try {
                    projectRoot = new IntellijFileContext(project);
                } catch (RuntimeException ex) {
                    //TODO logging here, the project was not resolvable
                    tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Project structure cannot be determined atm. Please try again later.")));
                    return;
                }


                List<IUIRoutesRoutesFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot);
                if (routeFiles == null || routeFiles.isEmpty()) {
                    tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(MSG_NO_ROUTE_FOUND)));
                    return;
                }

                SwingRootParentNode rootNode = new SwingRootParentNode(LBL_ARTIFACTS);
                SwingRootParentNode routesHolder = new SwingRootParentNode(LBL_ROUTES);
                SwingRootParentNode modules = new SwingRootParentNode(LBL_MODULES);
                SwingRootParentNode components = new SwingRootParentNode(LBL_COMPONENTS);
                SwingRootParentNode services = new SwingRootParentNode(LBL_SERVICES);


                rootNode.add(routesHolder);
                rootNode.add(modules);
                rootNode.add(components);
                rootNode.add(services);

                DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
                buildRoutesTree(routesHolder);

                tree.setRootVisible(false);
                tree.setModel(newModel);

                //now we restore the expansion state

                if (expansionMonitor != null) {
                    expansionMonitor.restore();
                }


                /*found this usefule helper in the jetbrains intellij sources*/
                if (searchPath == null) {

                    expansionMonitor = TreeExpansionMonitor.install(tree);
                    MouseController<PsiRouteContext> contextMenuListener = new MouseController<>(tree, this::showPopup);
                    tree.addMouseListener(contextMenuListener);


                    searchPath = new TreeSpeedSearch(tree, this::convertToSearchableString);
                }

            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }

    @Nullable
    private String convertToSearchableString(TreePath treePath) {
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
    }

    private void buildRoutesTree(SwingRootParentNode routesHolder) {
        List<IUIRoutesRoutesFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot);

        routeFiles.forEach(ctx -> {

            String node = ctx instanceof NG_UIRoutesRoutesFileContext ? LBL_ANGLUAR_NG_ROUTES :
                    ctx instanceof TNAngularRoutesFileContext ? LBL_TN_DEC_ROUTES :
                            LBL_TN_DEC_UI_ROUTES;
            DefaultMutableTreeNode routes = SwingRouteTreeFactory.createRouteTrees(ctx, node);

            routesHolder.add(routes);
        });
    }

    @SuppressWarnings("unused")
    private void buildModulesTree(SwingRootParentNode routesHolder) {
        if (!projectRoot.getAngularRoot().isPresent()) {
            return;
        }


        List<PsiFile> modules_tn = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), TN_DEC).stream()
                .flatMap(angRoot -> ModuleIndex.getAllAffectedFiles(projectRoot.getProject(), angRoot).stream()).collect(Collectors.toList());
        List<PsiFile> modules_ng = AngularIndex.getAllAffectedRoots(projectRoot.getProject(), NG).stream()
                .flatMap(angRoot -> ModuleIndex.getAllAffectedFiles(projectRoot.getProject(), angRoot).stream()).collect(Collectors.toList());



        /*modules.forEach(ctx -> {

            String node = ctx instanceof NG_UIRoutesRoutesFileContext ? "Angluar NG  Routes" :
                    ctx instanceof TNAngularRoutesFileContext ? "TN Dec Routes" :
                            "TN Dec UI Routes";
            DefaultMutableTreeNode routes = SwingRouteTreeFactory.createRouteTrees(ctx, node);

            routesHolder.add(routes);
        });*/
    }


    private void goToRouteDcl(PsiRouteContext foundContext) {
        openEditor(foundContext);
    }


    private void goToComponent(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (Strings.isNullOrEmpty(route.getComponentPath())) {
            Messages.showErrorDialog(this.tree.getRootPane(),
                    MSG_NO_COMP_PRES_CHECK_ROUTE, ERR_OCCURRED);
            return;
        }
        Path componentPath = Paths.get(route.getComponentPath());
        Path parent = Paths.get(Objects.requireNonNull(foundContext.getElement().getContainingFile().getParent()).getVirtualFile().getPath());
        Path rel = parent.relativize(componentPath);
        VirtualFile virtualFile = foundContext.getElement().getContainingFile().getParent().getVirtualFile().findFileByRelativePath(normalizePath(rel.toString()) + ".ts");
        if(virtualFile != null) {
            openEditor(new IntellijFileContext(foundContext.getElement().getProject(), virtualFile));
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
        copyToClipboard(stringSelection);
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
        String sRef;

        if (route.getOriginContext().equals(TNUIRoutesFileContext.class)) {
            sRef = HREF_TN_UIROUTES;
        } else if (route.getOriginContext().equals(TNAngularRoutesFileContext.class)) {
            sRef = HREF_TN;
        } else {
            sRef = HREF_NG_UI_ROUTES;
        }
        stringSelection = new StringSelection(String.format(sRef,route.getUrl(), route.getRouteVarName()));

        copyToClipboard(stringSelection);
    }


}
