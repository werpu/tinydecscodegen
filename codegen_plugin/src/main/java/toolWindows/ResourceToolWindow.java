package toolWindows;

import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ThreeComponentsSplitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

import static actions_all.shared.Labels.*;
import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.utils.IntellijUtils.*;
import static supportive.utils.StringUtils.makeVarName;
import static supportive.utils.SwingUtils.copyToClipboard;
import static supportive.utils.SwingUtils.openEditor;
import static toolWindows.SwingRouteTreeFactory.createComponentsTree;
import static toolWindows.SwingRouteTreeFactory.createModulesTree;

public class ResourceToolWindow implements ToolWindowFactory, Disposable {

    private Tree modulesTree = new Tree();
    private Tree componentsTree = new Tree();
    private Tree otherResourcesTree = new Tree();

    private gui.ResourceToolWindow contentPanel = new gui.ResourceToolWindow();
    //private gui.AngularStructureToolWindow contentPanel = new gui.AngularStructureToolWindow();


    private IntellijFileContext projectRoot = null;

    public ResourceToolWindow() {
        final Icon ng = IconLoader.getIcon("/images/ng.png");


        modulesTree.setCellRenderer(new ContextNodeRenderer());
        registerPopup(modulesTree);

        componentsTree.setCellRenderer(new ContextNodeRenderer());
        registerPopup(componentsTree);
        otherResourcesTree.setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResourcesTree);

        NodeKeyController<NgModuleFileContext> moduleKeyCtrl = createDefaultKeyController(modulesTree);
        NodeKeyController<ComponentFileContext> componentKeyController = createDefaultKeyController(componentsTree);
        NodeKeyController<IAngularFileContext> otherResourcesKeyCtrl = createDefaultKeyController(otherResourcesTree);


        modulesTree.addKeyListener(moduleKeyCtrl);
        componentsTree.addKeyListener(componentKeyController);
        otherResourcesTree.addKeyListener(otherResourcesKeyCtrl);
    }

    public void registerPopup(Tree tree) {
        MouseController<IAngularFileContext> contextMenuListener = new MouseController<>(tree, this::showPopup);
        tree.addMouseListener(contextMenuListener);
    }

    @NotNull
    public <T extends IAngularFileContext> NodeKeyController<T> createDefaultKeyController(Tree tree) {
        return new NodeKeyController<>(tree,
                this::gotToFile, this::goToParentModule, this::copyResourceName);
    }

    private void copyResourceName(IAngularFileContext fileContext) {
        copyToClipboard(fileContext.getDisplayName());
    }

    private void copyResourceClass(IAngularFileContext fileContext) {
        if (fileContext instanceof AngularResourceContext) {
            AngularResourceContext ctx = (AngularResourceContext) fileContext;
            copyToClipboard(ctx.getClazzName());
        }
    }

    private void copyServiceInject(IAngularFileContext fileContext) {
        if (fileContext instanceof AngularResourceContext) {
            AngularResourceContext ctx = (AngularResourceContext) fileContext;
            String clazzName = ctx.getClazzName();
            String varName = makeVarName(clazzName);

            String serciceInject = String.format("@Inject(%s) private %s: %s ", clazzName, varName, clazzName);
            copyToClipboard(serciceInject);
        }
    }

    private void goToParentModule(IAngularFileContext fileContext) {
        openEditor(fileContext.getParentModule().getResourceRoot());
    }

    private void gotToFile(IAngularFileContext fileContext) {
        openEditor(new IntellijFileContext(fileContext.getPsiFile().getProject(), fileContext.getVirtualFile()));
    }

    private void showPopup(IAngularFileContext foundContext, MouseEvent ev) {

        PopupBuilder builder = new PopupBuilder();
        builder
                .withMenuItem(LBL_GO_TO_REGISTRATION, actionEvent -> goToParentModule(foundContext))
                .withMenuItem(LBL_GO_TO_RESOURCE, actionEvent -> gotToFile(foundContext))
                .withSeparator()
                .withMenuItem(LBL_COPY_RESOURCE_NAME, actionEvent -> copyResourceName(foundContext))
                .withMenuItem(LBL_COPY_RESOURCE_CLASS, actionEvent -> copyResourceClass(foundContext));

        if (foundContext instanceof ServiceContext) {
            builder.withSeparator();
            builder.withMenuItem(LBL_COPY_SERCICE_INJECTION, actionEvent -> copyServiceInject(foundContext));
        }
        builder.show(ev.getComponent(), ev.getX(), ev.getY());

    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {


        onFileChange(project, () -> refreshContent(project));

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(true, true);
        refreshContent(project);


        ThreeComponentsSplitter myThreeComponentsSplitter = new ThreeComponentsSplitter(false, true) {
            @Override
            public void doLayout() {
                super.doLayout();

            }
        };
        Disposer.register(this, myThreeComponentsSplitter);
        JScrollPane jPanelLeft = contentPanel.getJPanelLeft();
        myThreeComponentsSplitter.setFirstComponent(jPanelLeft);
        JScrollPane jPanelMiddle = contentPanel.getJPanelMiddle();
        myThreeComponentsSplitter.setInnerComponent(jPanelMiddle);
        JScrollPane jPanelRight = contentPanel.getJPanelRight();
        myThreeComponentsSplitter.setLastComponent(jPanelRight);


        myThreeComponentsSplitter.setShowDividerControls(false);
        myThreeComponentsSplitter.setDividerWidth(1);

        myThreeComponentsSplitter.setOrientation(false);


        jPanelLeft.setViewportView(modulesTree);
        jPanelMiddle.setViewportView(componentsTree);
        jPanelRight.setViewportView(otherResourcesTree);


        toolWindowPanel.setContent(myThreeComponentsSplitter);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        invokeLater(() -> {
            int origWidth = toolWindow.getComponent().getRootPane().getSize().width;
            myThreeComponentsSplitter.setFirstSize(Math.round(origWidth / 3));
            myThreeComponentsSplitter.setLastSize(Math.round(origWidth / 3));
            myThreeComponentsSplitter.doLayout();
        });


        if (toolWindow instanceof ToolWindowEx) {
            final CommonActionsManager actionsManager = CommonActionsManager.getInstance();
            AnAction[] titleActions = actions(
                    actionGroup(
                            actionsManager.createExpandAllHeaderAction(modulesTree),
                            actionsManager.createCollapseAllHeaderAction(modulesTree)
                    ),

                    actionGroup(
                            actionsManager.createExpandAllHeaderAction(componentsTree),
                            actionsManager.createCollapseAllHeaderAction(componentsTree)
                    ),

                    actionGroup(
                            actionsManager.createExpandAllHeaderAction(otherResourcesTree),
                            actionsManager.createCollapseAllHeaderAction(otherResourcesTree)
                    )
            );
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);
        }

    }


    private void refreshContent(Project project) {
        invokeLater(() -> {
            try {
                assertNotInUse(project);

                buildTree(modulesTree, LBL_MODULES, this::buildModulesTree);
                buildTree(componentsTree, LBL_COMPONENTS, this::buildComponentsTree);
                buildTree(otherResourcesTree, LBL_RESOURCES, this::buildResourcesTree);

            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }

    private void buildModulesTree(SwingRootParentNode parentTree) {
        ContextFactory contextFactory = ContextFactory.getInstance(projectRoot);
        List<NgModuleFileContext> modules = contextFactory.getModules(projectRoot, TN_DEC);
        List<NgModuleFileContext> modules2 = contextFactory.getModules(projectRoot, NG);

        DefaultMutableTreeNode nodes = createModulesTree(modules, LBL_TN_DEC_MODULES);
        parentTree.add(nodes);
        nodes = createModulesTree(modules2, LBL_NG_MODULES);
        parentTree.add(nodes);

    }


    private void buildResourcesTree(SwingRootParentNode parentTree) {
        ContextFactory contextFactory = ContextFactory.getInstance(projectRoot);
        ResourceFilesContext itemsTn = contextFactory.getProjectResources(projectRoot, TN_DEC);
        ResourceFilesContext itemsNg = contextFactory.getProjectResources(projectRoot, NG);

        DefaultMutableTreeNode nodes = SwingRouteTreeFactory.createResourcesTree(itemsTn, LBL_TN_DEC_RESOURCES);
        parentTree.add(nodes);
        nodes = SwingRouteTreeFactory.createResourcesTree(itemsNg, LBL_NG_RESOURCES);
        parentTree.add(nodes);

    }

    private void buildComponentsTree(SwingRootParentNode parentTree) {
        ContextFactory contextFactory = ContextFactory.getInstance(projectRoot);
        List<ComponentFileContext> components = contextFactory.getComponents(projectRoot, TN_DEC);
        List<ComponentFileContext> components2 = contextFactory.getComponents(projectRoot, NG);

        DefaultMutableTreeNode nodes = createComponentsTree(components, LBL_TN_DEC_COMPONENTS);
        parentTree.add(nodes);
        nodes = createComponentsTree(components2, LBL_NG_COMPONENTS);
        parentTree.add(nodes);
    }

    private void assertNotInUse(Project project) {
        projectRoot = new IntellijFileContext(project);
    }

    void buildTree(Tree target, String label, Consumer<SwingRootParentNode> c) {
        SwingRootParentNode rootNode = new SwingRootParentNode(label);
        c.accept(rootNode);
        DefaultTreeModel newModel = new DefaultTreeModel(rootNode);
        target.setRootVisible(false);
        target.setModel(newModel);
    }


    @Override
    public void init(ToolWindow window) {
        System.out.println("init");
    }

    @Override
    public void dispose() {

    }
}
