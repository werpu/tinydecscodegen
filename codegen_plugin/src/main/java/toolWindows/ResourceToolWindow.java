package toolWindows;

import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
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
import supportive.fs.common.NgModuleFileContext;

import javax.swing.*;

public class ResourceToolWindow implements ToolWindowFactory, Disposable {

    private Tree modulesTree = new Tree();
    private Tree componentsTree = new Tree();
    private Tree otherResourcesTree = new Tree();

    private gui.ResourceToolWindow contentPanel = new gui.ResourceToolWindow();

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

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true);


        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

        ThreeComponentsSplitter myThreeComponentsSplitter = new ThreeComponentsSplitter() {
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

        jPanelLeft.setViewportView(modulesTree);
        jPanelMiddle.setViewportView(componentsTree);
        jPanelRight.setViewportView(otherResourcesTree);

        panel.setContent(myThreeComponentsSplitter);
        panel.setBackground(UIUtil.getEditorPaneBackground());

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

            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile fileOrDirectory) {

            }
        });
    }

    @Override
    public void init(ToolWindow window) {

    }

    @Override
    public void dispose() {

    }
}
