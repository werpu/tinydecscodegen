package toolWindows;

import com.intellij.build.BuildTreeConsoleView;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ThreeComponentsSplitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.*;

import javax.swing.*;
import java.awt.*;

import static supportive.utils.StringUtils.normalizePath;

public class ResourceToolWindow implements ToolWindowFactory, Disposable {

    private Tree tree = new Tree();

    private gui.ResourceToolWindow contentPanel = new gui.ResourceToolWindow();

    public ResourceToolWindow() {
        final Icon ng = IconLoader.getIcon("/images/ng.png");


        tree.setCellRenderer(new ContextNodeRenderer());


    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true);

        //contentPanel.getJPanelLeft().setViewportView(tree);
        //contentPanel.getJPanelMiddle().setViewportView(tree);
        //contentPanel.getJPanelRight().setViewportView(tree);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

        //TODO check com.intellij.openapi.ui.ThreeComponentsSplitter and its usage

        ThreeComponentsSplitter myThreeComponentsSplitter = new ThreeComponentsSplitter() {
            @Override
            public void doLayout() {
                super.doLayout();

            }
        };
        Disposer.register(this, myThreeComponentsSplitter);
        myThreeComponentsSplitter.setFirstComponent(contentPanel.getJPanelLeft());
        myThreeComponentsSplitter.setInnerComponent(contentPanel.getJPanelMiddle());
        myThreeComponentsSplitter.setLastComponent(contentPanel.getJPanelRight());

        panel.setContent(myThreeComponentsSplitter);
        panel.setBackground(UIUtil.getEditorPaneBackground());

    }

    @Override
    public void init(ToolWindow window) {

    }

    @Override
    public void dispose() {

    }
}
