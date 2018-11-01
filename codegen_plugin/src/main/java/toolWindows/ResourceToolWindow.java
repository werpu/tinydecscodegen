package toolWindows;

import com.google.common.collect.Streams;
import com.intellij.icons.AllIcons;
import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ThreeComponentsSplitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import supportive.fs.common.*;
import supportive.utils.IntellijRunUtils;
import supportive.utils.SearchableTree;
import supportive.utils.StringUtils;
import supportive.utils.TimeoutWorker;
import toolWindows.supportive.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static actions_all.shared.Labels.*;
import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.utils.IntellijRunUtils.*;
import static supportive.utils.StringUtils.makeVarName;
import static supportive.utils.SwingUtils.copyToClipboard;
import static supportive.utils.SwingUtils.openEditor;
import static toolWindows.supportive.SwingRouteTreeFactory.createModulesTree;


public class ResourceToolWindow implements ToolWindowFactory, Disposable {


    public static final int ACTION_PADDING = 3;
    private SearchableTree<NgModuleFileContext> modules = new SearchableTree();
    private SearchableTree<ResourceFilesContext> otherResources = new SearchableTree();
    private SearchableTree<ResourceFilesContext> otherResourcesModule = new SearchableTree();


    private gui.ResourceToolWindow contentPanel = new gui.ResourceToolWindow();
    private ThreeComponentsSplitter myThreeComponentsSplitter;

    private IntellijFileContext projectRoot = null;

    TimeoutWorker resourcesWatcher;

    ToolWindow toolWindow;

    public ResourceToolWindow() {
        final Icon ng = IconLoader.getIcon("/images/ng.png");


        modules.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(modules.getTree());

        otherResources.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResources.getTree());
        otherResourcesModule.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResourcesModule.getTree());

        Consumer<IAngularFileContext> gotToFile = this::gotToFile;
        Consumer<IAngularFileContext> goToParentModule = this::goToParentModule;
        Consumer<IAngularFileContext> copyResourceName = this::copyResourceName;

        modules.createDefaultKeyController(gotToFile, goToParentModule, copyResourceName);
        otherResources.createDefaultKeyController(gotToFile, goToParentModule, copyResourceName);
        otherResourcesModule.createDefaultKeyController(gotToFile, goToParentModule, copyResourceName);


        modules.createDefaultClickHandlers((fileSelected) -> {

        }, gotToFile);
        otherResources.createDefaultClickHandlers(NOOP_CONSUMER, gotToFile);
        otherResourcesModule.createDefaultClickHandlers(NOOP_CONSUMER, gotToFile);

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

            String serviceInject = String.format("@Inject(%s) private %s: %s ", clazzName, varName, clazzName);
            copyToClipboard(serviceInject);
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
                .withMenuItem(LBL_GO_TO_RESOURCE, actionEvent -> gotToFile(foundContext))
                .withMenuItem(LBL_GO_TO_REGISTRATION, actionEvent -> goToParentModule(foundContext))
                .withSeparator();
        if (foundContext instanceof ServiceContext) {

            builder.withMenuItem(LBL_COPY_SERCICE_INJECTION, actionEvent -> copyServiceInject(foundContext));
            builder.withSeparator();
        }

        builder.withMenuItem(LBL_COPY_RESOURCE_NAME, actionEvent -> copyResourceName(foundContext))
                .withMenuItem(LBL_COPY_RESOURCE_CLASS, actionEvent -> copyResourceClass(foundContext));


        builder.show(ev.getComponent(), ev.getX(), ev.getY());

    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        this.toolWindow = toolWindow;
        assertNotInUse(project);
        try {
            initWatcherThread(project);
        } catch (IndexNotReadyException ex) {
            runReadSmart(project, () -> initWatcherThread(project));
        }

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(true, true);


        myThreeComponentsSplitter = new ThreeComponentsSplitter(false, true) {
            @Override
            public void doLayout() {
                super.doLayout();

            }
        };
        Disposer.register(this, myThreeComponentsSplitter);
        JScrollPane jPanelLeft = contentPanel.getJPanelLeft();
        myThreeComponentsSplitter.setFirstComponent(wrapPanelBorderLayout(jPanelLeft));
        JScrollPane jPanelMiddle = contentPanel.getJPanelMiddle();
        myThreeComponentsSplitter.setInnerComponent(wrapPanelBorderLayout(jPanelMiddle));
        JScrollPane jPanelRight = contentPanel.getJPanelRight();
        myThreeComponentsSplitter.setLastComponent(wrapPanelBorderLayout(jPanelRight));


        myThreeComponentsSplitter.setShowDividerControls(false);
        myThreeComponentsSplitter.setDividerWidth(1);

        myThreeComponentsSplitter.setOrientation(false);


        jPanelLeft.setViewportView(modules.getTree());
        jPanelMiddle.setViewportView(otherResources.getTree());
        jPanelRight.setViewportView(otherResourcesModule.getTree());


        toolWindowPanel.setContent(myThreeComponentsSplitter);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        layout(toolWindow, myThreeComponentsSplitter);


        setupActionBar(toolWindow, toolWindowPanel);

        modules.makeSearchable(this::showPopup);
        otherResources.makeSearchable(this::showPopup);
        otherResourcesModule.makeSearchable(this::showPopup);

        IntellijRunUtils.onEditorChange(project, this::editorSwitched);


    }

    @NotNull
    private JPanel wrapPanelBorderLayout(JScrollPane jPanelLeft) {
        JPanel wrapper = new JPanel();

        wrapper.setLayout(new BorderLayout());
        wrapper.add(jPanelLeft, BorderLayout.CENTER);
        return wrapper;
    }

    private void initWatcherThread(@NotNull Project project) {
        resourcesWatcher = new TimeoutWorker(worker -> refreshContent());
        resourcesWatcher.start();
        IntellijRunUtils.onFileChange(project, () -> onEditChange());
    }

    private void onEditChange() {
        resourcesWatcher.notifyOfChange();
    }

    private void setupActionBar(@NotNull ToolWindow toolWindow, SimpleToolWindowPanel panel) {
        if (toolWindow instanceof ToolWindowEx) {
            final CommonActionsManager actionsManager = CommonActionsManager.getInstance();
            AnAction[] titleActions = IntellijRunUtils.actions(
                    new AnAction("Reload All", "Reload All Views", AllIcons.Actions.ForceRefresh) {
                        @Override
                        public void actionPerformed(AnActionEvent e) {
                            resourcesWatcher.stop();
                            refreshContent();
                        }
                    },



                    new AnAction("Expand All", "Expand All Views", AllIcons.Actions.Expandall) {
                        ActionGroup expandAll = IntellijRunUtils.actionGroup(
                                actionsManager.createExpandAllHeaderAction(modules.getTree()),
                                actionsManager.createExpandAllHeaderAction(otherResources.getTree()),
                                actionsManager.createExpandAllHeaderAction(otherResourcesModule.getTree())
                                );

                        @Override
                        public void actionPerformed(AnActionEvent e) {

                            Arrays.stream(expandAll.getChildren(e)).forEach(a -> a.actionPerformed(e));
                        }
                    },

                    new AnAction("Collapse All", "Collapse All Views", AllIcons.Actions.Collapseall) {
                        ActionGroup collapseAll = IntellijRunUtils.actionGroup(
                                actionsManager.createCollapseAllHeaderAction(modules.getTree()),
                                actionsManager.createCollapseAllHeaderAction(otherResources.getTree()),
                                actionsManager.createCollapseAllHeaderAction(otherResourcesModule.getTree())
                        );

                        @Override
                        public void actionPerformed(AnActionEvent e) {

                            Arrays.stream(collapseAll.getChildren(e)).forEach(a -> a.actionPerformed(e));
                        }
                    }
            );
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);


            ActionToolbar actions = ActionManager.getInstance().createActionToolbar("Module Actions", IntellijRunUtils.actionGroup(
                    actionsManager.createExpandAllHeaderAction(modules.getTree()),
                    actionsManager.createCollapseAllHeaderAction(modules.getTree())
            ), false);

            actions.setTargetComponent(myThreeComponentsSplitter.getFirstComponent());
            myThreeComponentsSplitter.getFirstComponent().add(wrapActionBar(actions.getComponent()), BorderLayout.WEST);
            myThreeComponentsSplitter.getFirstComponent().add(wrapHeader(new JLabel("Modules")), BorderLayout.NORTH);

            actions = ActionManager.getInstance().createActionToolbar("Resource Actions", IntellijRunUtils.actionGroup(
                    actionsManager.createExpandAllHeaderAction(otherResources.getTree()),
                    actionsManager.createCollapseAllHeaderAction(otherResources.getTree())
            ), false);

            actions.setTargetComponent(myThreeComponentsSplitter.getInnerComponent());
            myThreeComponentsSplitter.getInnerComponent().add(wrapActionBar(actions.getComponent()), BorderLayout.WEST);
            myThreeComponentsSplitter.getInnerComponent().add(wrapHeader(new JLabel("Resources")), BorderLayout.NORTH);


            actions = ActionManager.getInstance().createActionToolbar("Editor Resource Actions", IntellijRunUtils.actionGroup(
                    actionsManager.createExpandAllHeaderAction(otherResourcesModule.getTree()),
                    actionsManager.createCollapseAllHeaderAction(otherResourcesModule.getTree())
            ), false);

            actions.setTargetComponent(myThreeComponentsSplitter.getLastComponent());
            myThreeComponentsSplitter.getLastComponent().add(wrapActionBar(actions.getComponent()), BorderLayout.WEST);
            myThreeComponentsSplitter.getLastComponent().add(wrapHeader(new JLabel("Current Resources")), BorderLayout.NORTH);



        }
    }

    JPanel wrapActionBar(Component component) {
        JPanel wrapper = new JPanel();
        wrapper.setBorder(BorderFactory.createCompoundBorder( new MatteBorder(0,0,0, 1, Color.LIGHT_GRAY), new EmptyBorder(ACTION_PADDING, ACTION_PADDING, ACTION_PADDING, ACTION_PADDING)));
        wrapper.setLayout(new BorderLayout());
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    JPanel wrapHeader(JLabel component) {
        JPanel wrapper = new JPanel();
        Font f = component.getFont();
        component.setFont(new Font(f.getName(), f.getStyle(), f.getSize() - 1));
        wrapper.setBorder(BorderFactory.createCompoundBorder( new MatteBorder(0,0,1, 0, Color.LIGHT_GRAY), new EmptyBorder(2, 7, 2, 7)));
        wrapper.setLayout(new BorderLayout());
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private void layout(@NotNull ToolWindow toolWindow, ThreeComponentsSplitter myThreeComponentsSplitter) {
        //setTimeout(() -> {
            IntellijRunUtils.invokeLater(() -> {
                int origWidth = toolWindow.getComponent().getParent().getWidth();
                myThreeComponentsSplitter.setFirstSize(Math.round(origWidth / 3));
                myThreeComponentsSplitter.setLastSize(Math.round(origWidth / 3));
                myThreeComponentsSplitter.doLayout();
            });

    }


    private void editorSwitched(FileEditorManagerEvent evt) {
        FileEditor editor = evt.getNewEditor();
        Project project = evt.getManager().getProject();
        editorTreeRefresh(editor, project);

    }

    private void editorTreeRefresh(FileEditor editor, Project project) {
        if (editor == null) {
            return;
        }
        smartInvokeLater(project, () -> {
            IntellijRunUtils.readAction(() -> {
                VirtualFile currentFile = editor.getFile();

                IntellijFileContext fileContext = new IntellijFileContext(project, currentFile);

                Optional<NgModuleFileContext> ret = getNearestModule(fileContext);

                if (ret.isPresent()) {
                    otherResourcesModule.filterTree(ret.get().getFolderPath(), LBL_RESOURCES + "[" + ret.get().getModuleName() + "]");
                }
                otherResourcesModule.restoreExpansion();
            });
        });

    }

    private Optional<NgModuleFileContext> getNearestModule(IntellijFileContext fileContext) {
        IntellijFileContext project = new IntellijFileContext(fileContext.getProject());
        ContextFactory ctxf = ContextFactory.getInstance(project);
        String filterStr = StringUtils.normalizePath(fileContext.getFolderPath());
        return Streams.concat(
                ctxf.getModulesFor(project, TN_DEC, filterStr).stream(),
                ctxf.getModulesFor(project, NG, filterStr).stream()
        ).reduce((el1, el2) -> el1.getFolderPath().length() > el2.getFolderPath().length() ? el1 : el2);
    }


    AtomicBoolean refreshRunning = new AtomicBoolean(Boolean.FALSE);


    public void refreshContent() {
        if (toolWindow == null || !toolWindow.isVisible()) {
            return;
        }
        final Project project = projectRoot.getProject();
        runAsync(backgroundTask(project, "Reloading Resource View", progress -> fullRefresh(project)));
    }

    private void fullRefresh(Project project) {
        if (refreshRunning.get()) {
            return;
        }

        refreshRunning.set(true);
        try {

            DumbService.getInstance(project).runReadActionInSmartMode(() -> {

                assertNotInUse(project);


                ContextFactory contextFactory = ContextFactory.getInstance(projectRoot);


                ResourceFilesContext itemsTn = contextFactory.getProjectResources(projectRoot, TN_DEC);
                ResourceFilesContext itemsNg = contextFactory.getProjectResources(projectRoot, NG);


                modules.refreshContent(LBL_MODULES, this.buildModulesTree(itemsTn.getModules(), itemsNg.getModules()));
                otherResources.refreshContent(LBL_RESOURCES, this.buildResourcesTree(itemsTn, itemsNg));
                otherResourcesModule.refreshContent(LBL_RESOURCES, this.buildResourcesTree(itemsTn, itemsNg));


                Arrays.<Runnable>asList(() -> modules.filterTree("", LBL_MODULES),
                        () -> otherResources.filterTree("", LBL_RESOURCES),
                        () -> {
                            otherResourcesModule.filterTree("", LBL_MODULES);
                            FileEditor editor = FileEditorManagerImpl.getInstance(project).getSelectedEditor();
                            editorTreeRefresh(editor, project);
                        }).parallelStream()
                        .forEach(runnable -> runnable.run());


            });


        } catch (IndexNotReadyException exception) {
            //nnop we try another time
        } finally {
            refreshRunning.set(false);
            modules.restoreExpansion();
            otherResources.restoreExpansion();
            otherResourcesModule.restoreExpansion();
        }

    }


    private void buildModulesTree(SwingRootParentNode parentTree, List<NgModuleFileContext> modules, List<NgModuleFileContext> modules2) {
        DefaultMutableTreeNode nodes = createModulesTree(modules, LBL_TN_DEC_MODULES);
        parentTree.add(nodes);
        nodes = createModulesTree(modules2, LBL_NG_MODULES);
        parentTree.add(nodes);

    }

    public Consumer<SwingRootParentNode> buildModulesTree(List<NgModuleFileContext> itemsTn, List<NgModuleFileContext> itemsNg) {
        return (parentTree) ->
                buildModulesTree(parentTree, itemsTn, itemsNg);
    }


    public Consumer<SwingRootParentNode> buildResourcesTree(ResourceFilesContext itemsTn, ResourceFilesContext itemsNg) {
        return (parentTree) ->
                buildResourcesTree(parentTree, itemsTn, itemsNg);
    }

    private void buildResourcesTree(SwingRootParentNode parentTree, ResourceFilesContext itemsTn, ResourceFilesContext itemsNg) {


        DefaultMutableTreeNode nodes = SwingRouteTreeFactory.createResourcesTree(itemsTn, LBL_TN_DEC_RESOURCES);
        parentTree.add(nodes);
        nodes = SwingRouteTreeFactory.createResourcesTree(itemsNg, LBL_NG_RESOURCES);
        parentTree.add(nodes);

    }

    private void assertNotInUse(Project project) {
        projectRoot = new IntellijFileContext(project);
    }


    @Override
    public void init(ToolWindow window) {

    }

    @Override
    public void dispose() {

    }
}
