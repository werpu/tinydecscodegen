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
import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.*;
import supportive.utils.SearchableTree;
import toolWindows.supportive.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.KeyListener;
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
import static toolWindows.supportive.SwingRouteTreeFactory.createComponentsTree;
import static toolWindows.supportive.SwingRouteTreeFactory.createModulesTree;






public class ResourceToolWindow implements ToolWindowFactory, Disposable {

    
    private SearchableTree<NgModuleFileContext> modules = new SearchableTree();
    private SearchableTree<ComponentFileContext> components = new SearchableTree();
    private SearchableTree<ResourceFilesContext> otherResources = new SearchableTree();;
    


    private gui.ResourceToolWindow contentPanel = new gui.ResourceToolWindow();
    //private gui.AngularStructureToolWindow contentPanel = new gui.AngularStructureToolWindow();


    private IntellijFileContext projectRoot = null;

    public ResourceToolWindow() {
        final Icon ng = IconLoader.getIcon("/images/ng.png");


        modules.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(modules.getTree());

        components.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(components.getTree());
        otherResources.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResources.getTree());

        NodeKeyController<NgModuleFileContext> moduleKeyCtrl = createDefaultKeyController(modules.getTree());
        NodeKeyController<ComponentFileContext> componentKeyController = createDefaultKeyController(components.getTree());
        NodeKeyController<IAngularFileContext> otherResourcesKeyCtrl = createDefaultKeyController(otherResources.getTree());


        modules.addKeyListener(moduleKeyCtrl);
        components.addKeyListener(componentKeyController);
        otherResources.addKeyListener(otherResourcesKeyCtrl);


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
        copyToClipboard(fileContext.getResourceName());
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


        jPanelLeft.setViewportView(modules.getTree());
        jPanelMiddle.setViewportView(components.getTree());
        jPanelRight.setViewportView(otherResources.getTree());


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
                            actionsManager.createExpandAllHeaderAction(modules.getTree()),
                            actionsManager.createCollapseAllHeaderAction(modules.getTree())
                    ),

                    actionGroup(
                            actionsManager.createExpandAllHeaderAction(components.getTree()),
                            actionsManager.createCollapseAllHeaderAction(components.getTree())
                    ),

                    actionGroup(
                            actionsManager.createExpandAllHeaderAction(otherResources.getTree()),
                            actionsManager.createCollapseAllHeaderAction(otherResources.getTree())
                    )
            );
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);
        }

        modules.makeSearchable(this::showPopup);
        components.makeSearchable(this::showPopup);
        otherResources.makeSearchable(this::showPopup);


    }






    private void refreshContent(Project project) {
        invokeLater(() -> {
            try {
                assertNotInUse(project);

                modules.refreshContent(LBL_MODULES, this::buildModulesTree);
                components.refreshContent(LBL_COMPONENTS, this::buildComponentsTree);
                otherResources.refreshContent(LBL_RESOURCES, this::buildResourcesTree);

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



    @Override
    public void init(ToolWindow window) {
        System.out.println("init");
    }

    @Override
    public void dispose() {

    }
}
