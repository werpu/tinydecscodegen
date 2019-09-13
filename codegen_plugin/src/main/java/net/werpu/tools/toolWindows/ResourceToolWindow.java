/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.toolWindows;

import com.intellij.icons.AllIcons;
import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ThreeComponentsSplitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.*;
import net.werpu.tools.supportive.utils.IntellijRunUtils;
import net.werpu.tools.supportive.utils.IntellijUtils;
import net.werpu.tools.supportive.utils.SearchableTree;
import net.werpu.tools.supportive.utils.TimeoutWorker;
import net.werpu.tools.toolWindows.supportive.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.intellij.util.ui.tree.TreeUtil.expandAll;
import static net.werpu.tools.actions_all.shared.Labels.*;
import static net.werpu.tools.supportive.fs.common.AngularVersion.NG;
import static net.werpu.tools.supportive.fs.common.AngularVersion.TN_DEC;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.*;
import static net.werpu.tools.supportive.utils.StringUtils.makeVarName;
import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;
import static net.werpu.tools.supportive.utils.SwingUtils.copyToClipboard;
import static net.werpu.tools.supportive.utils.SwingUtils.openEditor;
import static net.werpu.tools.toolWindows.supportive.SwingRouteTreeFactory.createModulesTree;


@Setter
@Getter
class ResourcePanels {

    private JScrollPane left;
    private JScrollPane middle1;
    private JScrollPane middle2;
    private JScrollPane right;

    private JPanel wrappedMiddle1;
    private JPanel wrappedMiddle2;

    ResourcePanels() {
        left = new JBScrollPane();
        middle1 = new JBScrollPane();
        middle2 = new JBScrollPane();
        right = new JBScrollPane();

    }

}

@CustomLog
public class ResourceToolWindow implements ToolWindowFactory, Disposable {

    private static final int ACTION_PADDING = 3;
    private SearchableTree<NgModuleFileContext> modules = new SearchableTree<>();
    private SearchableTree<ResourceFilesContext> otherResources = new SearchableTree<>();
    private SearchableTree<ResourceFilesContext> otherResourcesModule = new SearchableTree<>();
    private SearchableTree<ResourceFilesContext> otherResourcesActiveEditorModule = new SearchableTree<>();


    private ThreeComponentsSplitter myThreeComponentsSplitter;

    private IntellijFileContext projectRoot = null;

    private TimeoutWorker resourcesWatcher;

    private ToolWindow toolWindow;

    private ResourcePanels resourcePanels;
    private AtomicBoolean refreshRunning = new AtomicBoolean(Boolean.FALSE);

    public ResourceToolWindow() {


        modules.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(modules.getTree());

        otherResources.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResources.getTree());

        otherResourcesModule.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResourcesModule.getTree());

        otherResourcesActiveEditorModule.getTree().setCellRenderer(new ContextNodeRenderer());
        registerPopup(otherResourcesActiveEditorModule.getTree());

        Consumer<IAngularFileContext> gotToFile = this::gotToFile;
        Consumer<IAngularFileContext> goToParentModule = this::goToParentModule;
        Consumer<IAngularFileContext> copyResourceName = this::copyResourceName;

        Consumer<IAngularFileContext> updateSecTree = (fileSelected) -> invokeLater(() -> {
            //TODO filter the components tree
            if (fileSelected instanceof NgModuleFileContext) {
                updateSecondaryTree((NgModuleFileContext) fileSelected);
            }
        });
        Consumer<IAngularFileContext> moduleUpdateAndGoToFile = (fileSelected) -> invokeLater(() -> {
            //TODO filter the components tree
            if (fileSelected instanceof NgModuleFileContext) {
                updateSecondaryTree((NgModuleFileContext) fileSelected);
                gotToFile(fileSelected);
            }
        });


        modules.createDefaultKeyController(moduleUpdateAndGoToFile, goToParentModule, copyResourceName);
        otherResources.createDefaultKeyController(gotToFile, goToParentModule, copyResourceName);
        otherResourcesModule.createDefaultKeyController(gotToFile, goToParentModule, copyResourceName);
        otherResourcesActiveEditorModule.createDefaultKeyController(gotToFile, goToParentModule, copyResourceName);


        modules.createDefaultClickHandlers(updateSecTree, gotToFile);
        otherResources.createDefaultClickHandlers(NOOP_CONSUMER, gotToFile);
        otherResourcesModule.createDefaultClickHandlers(NOOP_CONSUMER, gotToFile);
        otherResourcesActiveEditorModule.createDefaultClickHandlers(NOOP_CONSUMER, gotToFile);


    }

    private void updateSecondaryTree(NgModuleFileContext fileSelected) {
        invokeLater(() -> {
            NgModuleFileContext module = fileSelected;
            String moduleFileName = module.getParent().isPresent() ? module.getParent().get().getFolderPath(): "";
            moduleFileName = normalizePath(moduleFileName);
            otherResourcesModule.filterTree(moduleFileName, "Resources " + "[" + module.getModuleName() + "]");
            runReadSmart(module.getProject(), () -> otherResourcesModule.restoreExpansion());
        });
    }

    public void registerPopup(Tree tree) {
        MouseController<IAngularFileContext> contextMenuListener = new MouseController<>(tree, this::showPopup);
        tree.addMouseListener(contextMenuListener);
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
        NgModuleFileContext parentModule = fileContext.getParentModule();
        if (parentModule != null) {
            openEditor(parentModule.getResourceRoot());
        } else {
            IntellijUtils.showInfoMessage("Parent module could not be found", "Info");
        }
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

        /**
         * if a file is not reachable anymore we need to update the tree
         */
        project.getMessageBus().connect().subscribe(GistListener.FILE_NOT_REACHABLE, virtualFile -> refreshContent());

        this.toolWindow = toolWindow;
        this.resourcePanels = new ResourcePanels();
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

        this.resourcePanels.setLeft(resourcePanels.getLeft());

        myThreeComponentsSplitter.setFirstComponent(wrapPanelBorderLayout(resourcePanels.getLeft()));


        JTabbedPane moduleTabs = new JBTabbedPane();
        JPanel wrappedMiddle2 = wrapPanelBorderLayout(resourcePanels.getMiddle2());
        moduleTabs.add("Module", wrappedMiddle2);
        JPanel wrappedMiddle1 = wrapPanelBorderLayout(resourcePanels.getMiddle1());
        moduleTabs.add("Project", wrappedMiddle1);

        resourcePanels.setWrappedMiddle1(wrappedMiddle1);
        resourcePanels.setWrappedMiddle2(wrappedMiddle2);

        myThreeComponentsSplitter.setInnerComponent(moduleTabs);
        JScrollPane jPanelRight = new JBScrollPane();
        myThreeComponentsSplitter.setLastComponent(wrapPanelBorderLayout(jPanelRight));


        myThreeComponentsSplitter.setShowDividerControls(false);
        myThreeComponentsSplitter.setDividerWidth(1);

        myThreeComponentsSplitter.setOrientation(false);


        resourcePanels.getLeft().setViewportView(modules.getTree());


        resourcePanels.getMiddle1().setViewportView(otherResources.getTree());
        resourcePanels.getMiddle2().setViewportView(otherResourcesModule.getTree());


        jPanelRight.setViewportView(otherResourcesActiveEditorModule.getTree());


        toolWindowPanel.setContent(myThreeComponentsSplitter);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        layout(toolWindow, myThreeComponentsSplitter);


        setupActionBar(toolWindow, toolWindowPanel);

        modules.makeSearchable(this::showPopup);
        otherResources.makeSearchable(this::showPopup);
        otherResourcesModule.makeSearchable(this::showPopup);
        otherResourcesActiveEditorModule.makeSearchable(this::showPopup);

        onEditorChange(project, this::editorSwitched);


    }

    @NotNull
    private JPanel wrapPanelBorderLayout(Component jPanelLeft) {
        JPanel wrapper = new JPanel();

        wrapper.setLayout(new BorderLayout());
        wrapper.add(jPanelLeft, BorderLayout.CENTER);
        return wrapper;
    }

    private void initWatcherThread(@NotNull Project project) {
        refreshContent();
        resourcesWatcher = new TimeoutWorker(worker -> refreshContent());
        resourcesWatcher.start();
        onFileChange(project, this::onEditChange);
    }

    private void onEditChange() {
        resourcesWatcher.notifyOfChange();
    }

    private void setupActionBar(@NotNull ToolWindow toolWindow, SimpleToolWindowPanel panel) {
        if (toolWindow instanceof ToolWindowEx) {
            final CommonActionsManager actionsManager = CommonActionsManager.getInstance();
            AnAction[] titleActions = IntellijRunUtils.actions(
                    new AnAction("Reload All", "Reload All Views", AllIcons.Actions.Refresh) {
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
                                actionsManager.createExpandAllHeaderAction(otherResourcesModule.getTree()),
                                actionsManager.createExpandAllHeaderAction(otherResourcesActiveEditorModule.getTree())
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
                                actionsManager.createCollapseAllHeaderAction(otherResourcesModule.getTree()),
                                actionsManager.createCollapseAllHeaderAction(otherResourcesActiveEditorModule.getTree())
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
            Objects.requireNonNull(myThreeComponentsSplitter.getFirstComponent()).add(wrapActionBar(actions.getComponent()), BorderLayout.WEST);
            myThreeComponentsSplitter.getFirstComponent().add(wrapHeader(new JLabel("Modules")), BorderLayout.NORTH);


            actions = ActionManager.getInstance().createActionToolbar("Module Resource Actions", IntellijRunUtils.actionGroup(
                    actionsManager.createExpandAllHeaderAction(otherResourcesModule.getTree()),
                    actionsManager.createCollapseAllHeaderAction(otherResourcesModule.getTree())
            ), false);
            actions.setTargetComponent(resourcePanels.getMiddle2());
            resourcePanels.getWrappedMiddle2().add(actions.getComponent(), BorderLayout.WEST);


            actions = ActionManager.getInstance().createActionToolbar("Resource Actions", IntellijRunUtils.actionGroup(
                    actionsManager.createExpandAllHeaderAction(otherResources.getTree()),
                    actionsManager.createCollapseAllHeaderAction(otherResources.getTree())
            ), false);
            actions.setTargetComponent(resourcePanels.getMiddle1());
            resourcePanels.getWrappedMiddle1().add(actions.getComponent(), BorderLayout.WEST);


            actions = ActionManager.getInstance().createActionToolbar("Editor Resource Actions", IntellijRunUtils.actionGroup(
                    actionsManager.createExpandAllHeaderAction(otherResourcesActiveEditorModule.getTree()),
                    actionsManager.createCollapseAllHeaderAction(otherResourcesActiveEditorModule.getTree())
            ), false);

            actions.setTargetComponent(myThreeComponentsSplitter.getLastComponent());
            Objects.requireNonNull(myThreeComponentsSplitter.getLastComponent()).add(wrapActionBar(actions.getComponent()), BorderLayout.WEST);
            myThreeComponentsSplitter.getLastComponent().add(wrapHeader(new JLabel("Current Resources")), BorderLayout.NORTH);


        }
    }

    private JPanel wrapActionBar(Component component) {
        JPanel wrapper = new JPanel();
        wrapper.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 0, 1, JBColor.LIGHT_GRAY), JBUI.Borders.empty(ACTION_PADDING)));
        wrapper.setLayout(new BorderLayout());
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel wrapHeader(JLabel component) {
        JPanel wrapper = new JPanel();
        Font f = component.getFont();
        component.setFont(new Font(f.getName(), f.getStyle(), f.getSize() - 1));
        wrapper.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, JBColor.LIGHT_GRAY), JBUI.Borders.empty(8, 7)));
        wrapper.setLayout(new BorderLayout());
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private void layout(@NotNull ToolWindow toolWindow, ThreeComponentsSplitter myThreeComponentsSplitter) {
        //setTimeout(() -> {
        IntellijRunUtils.invokeLater(() -> {
            int origWidth = toolWindow.getComponent().getParent().getWidth();
            myThreeComponentsSplitter.setFirstSize(Math.round(origWidth / 3f));
            myThreeComponentsSplitter.setLastSize(Math.round(origWidth / 3f));
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
        smartInvokeLater(project, () -> readAction(() -> {
            VirtualFile currentFile = editor.getFile();

            IntellijFileContext fileContext = new IntellijFileContext(project, currentFile);

            Optional<NgModuleFileContext> ret = fileContext.getNearestModule();

            ret.ifPresent(ngModuleFileContext -> otherResourcesActiveEditorModule.filterTree(ngModuleFileContext.getFolderPath(), LBL_RESOURCES + "[" + ngModuleFileContext.getModuleName() + "]"));
            otherResourcesActiveEditorModule.restoreExpansion();
        }));

    }

    private void refreshContent() {
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
                otherResourcesActiveEditorModule.refreshContent(LBL_RESOURCES, this.buildResourcesTree(itemsTn, itemsNg));


                FutureTask<Boolean> run1 = newFutureRoTask(() -> {
                    modules.filterTree("", LBL_MODULES);
                    smartInvokeLater(project, () -> expandAll(modules.getTree()));

                    return Boolean.TRUE;
                });

                FutureTask<Boolean> run2 = newFutureRoTask(() -> {
                    otherResources.filterTree("", LBL_RESOURCES);
                    smartInvokeLater(project,
                            () -> expandAll(otherResources.getTree()));
                    return Boolean.TRUE;
                });

                FutureTask<Boolean> run3 = newFutureRoTask(() -> {
                    otherResourcesModule.filterTree("", LBL_RESOURCES);
                    smartInvokeLater(project, () -> expandAll(otherResourcesModule.getTree()));
                    return Boolean.TRUE;
                });

                FutureTask<Boolean> run4 = newFutureRoTask(() -> {
                    otherResourcesActiveEditorModule.filterTree("", LBL_MODULES);
                    FileEditor editor = FileEditorManagerImpl.getInstance(project).getSelectedEditor();
                    editorTreeRefresh(editor, project);
                    smartInvokeLater(project, () -> expandAll(otherResourcesActiveEditorModule.getTree()));
                    return Boolean.TRUE;
                });

                ExecutorService executor = Executors.newCachedThreadPool();

                executor.execute(run1);
                executor.execute(run2);
                executor.execute(run3);
                executor.execute(run4);
                executor.shutdown();

                /*try {
                    executor.awaitTermination(1000, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/


            });


        } catch (IndexNotReadyException exception) {
            //nnop we try another time
        } finally {
            refreshRunning.set(false);
            modules.restoreExpansion();
            otherResources.restoreExpansion();
            otherResourcesModule.restoreExpansion();
            otherResourcesActiveEditorModule.restoreExpansion();
        }

    }


    private void get(FutureTask<Boolean> run1) {
        try {
            run1.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    private void buildModulesTree(SwingRootParentNode parentTree, List<NgModuleFileContext> modules, List<NgModuleFileContext> modules2) {
        DefaultMutableTreeNode nodes = createModulesTree(modules, LBL_TN_DEC_MODULES);
        parentTree.add(nodes);
        nodes = createModulesTree(modules2, LBL_NG_MODULES);
        parentTree.add(nodes);

    }

    private Consumer<SwingRootParentNode> buildModulesTree(List<NgModuleFileContext> itemsTn, List<NgModuleFileContext> itemsNg) {
        return (parentTree) ->
                buildModulesTree(parentTree, itemsTn, itemsNg);
    }


    private Consumer<SwingRootParentNode> buildResourcesTree(ResourceFilesContext itemsTn, ResourceFilesContext itemsNg) {
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
