package toolWindows;

import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ThreeComponentsSplitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.ComponentFileContext;
import supportive.fs.common.ContextFactory;
import supportive.fs.common.IntellijFileContext;
import supportive.fs.common.NgModuleFileContext;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static actions_all.shared.Labels.*;
import static actions_all.shared.Messages.NO_PROJ_LATER;
import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.utils.IntellijUtils.getTsExtension;
import static supportive.utils.IntellijUtils.invokeLater;

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
        componentsTree.setCellRenderer(new ContextNodeRenderer());
        otherResourcesTree.setCellRenderer(new ContextNodeRenderer());

        NodeKeyController<NgModuleFileContext> moduleKeyCtrl = createDefaultKeyController(modulesTree);
        NodeKeyController<ComponentFileContext> componentKeyController = createDefaultKeyController(componentsTree);
        NodeKeyController<Object> otherResourcesKeyCtrl = createDefaultKeyController(otherResourcesTree);


        modulesTree.addKeyListener(moduleKeyCtrl);
        componentsTree.addKeyListener(componentKeyController);
        otherResourcesTree.addKeyListener(otherResourcesKeyCtrl);
    }

    @NotNull
    public <T> NodeKeyController<T> createDefaultKeyController(Tree tree) {
        return new NodeKeyController<T>(tree,
                this::gotToFile, this::goToParentModule, this::copyResourceName);
    }

    private void copyResourceName(Object psiRouteContext) {
    }

    private void goToParentModule(Object psiRouteContext) {
    }

    private void gotToFile(Object psiRouteContext) {
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {


        initChangeListener(project);

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

        myThreeComponentsSplitter.setFirstSize(200);
        myThreeComponentsSplitter.setLastSize(100);

        myThreeComponentsSplitter.setShowDividerControls(true);
        myThreeComponentsSplitter.setDividerWidth(10);
        myThreeComponentsSplitter.setOrientation(false);


        jPanelLeft.setViewportView(modulesTree);
        jPanelMiddle.setViewportView(componentsTree);
        jPanelRight.setViewportView(otherResourcesTree);

        myThreeComponentsSplitter.doLayout();

        toolWindowPanel.setContent(myThreeComponentsSplitter);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        if (toolWindow instanceof ToolWindowEx) {
            AnAction[] titleActions = new AnAction[]{
                    CommonActionsManager.getInstance().createExpandAllHeaderAction(modulesTree),
                    CommonActionsManager.getInstance().createCollapseAllHeaderAction(modulesTree),

                    CommonActionsManager.getInstance().createExpandAllHeaderAction(componentsTree),
                    CommonActionsManager.getInstance().createCollapseAllHeaderAction(componentsTree),

                    CommonActionsManager.getInstance().createExpandAllHeaderAction(otherResourcesTree),
                    CommonActionsManager.getInstance().createCollapseAllHeaderAction(otherResourcesTree)

            };
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);
        }

    }

    private void initChangeListener(Project project) {

        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                //TODO angular version dynamic depending on the project type
                if (!file.getName().endsWith(getTsExtension())) {
                    return;
                }

                //document listener which refreshes every time a route file changes
                getChangeListener().smartInvokeLater(() -> refreshContent(project));
                //TODO add the same check for modules which handle all the artifacts
            }

            private DumbService getChangeListener() {
                return DumbService.getInstance(project);
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        });
    }

    private void refreshContent(Project project) {
        invokeLater(() -> {
            try {
                assertNotInUse(project);
                buildTree(modulesTree, LBL_MODULES, this::buildModulesTree);
                buildTree(componentsTree, LBL_COMPONENTS, this::buildComponentsTree);

                //modulesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("aga")));
            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }

    private void buildModulesTree(SwingRootParentNode parentTree) {
        List<NgModuleFileContext> modules = ContextFactory.getInstance(projectRoot).getModules(projectRoot, TN_DEC);
        List<NgModuleFileContext> modules2 = ContextFactory.getInstance(projectRoot).getModules(projectRoot, NG);

        DefaultMutableTreeNode nodes = SwingRouteTreeFactory.createModulesTree(modules, LBL_TN_DEC_MODULES);
        parentTree.add(nodes);
        nodes = SwingRouteTreeFactory.createModulesTree(modules2, LBL_NG_MODULES);
        parentTree.add(nodes);

    }

    private void buildComponentsTree(SwingRootParentNode parentTree) {
        List<ComponentFileContext> components = ContextFactory.getInstance(projectRoot).getComponents(projectRoot, TN_DEC);
        List<ComponentFileContext> components2 = ContextFactory.getInstance(projectRoot).getComponents(projectRoot, NG);

        DefaultMutableTreeNode nodes = SwingRouteTreeFactory.createComponentsTree(components, LBL_TN_DEC_COMPONENTS);
        parentTree.add(nodes);
        nodes = SwingRouteTreeFactory.createComponentsTree(components2, LBL_NG_COMPONENTS);
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




    private void displayLater() {
        modulesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(NO_PROJ_LATER)));
        //componentsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(NO_PROJ_LATER)));
        //otherResourcesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(NO_PROJ_LATER)));
    }

    @Override
    public void init(ToolWindow window) {

    }

    @Override
    public void dispose() {

    }
}
