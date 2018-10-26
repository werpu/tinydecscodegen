package toolWindows;

import com.intellij.openapi.project.Project;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ResourceToolWindow implements ToolWindowFactory {

    private gui.ResourceToolWindow contentPanel = new gui.ResourceToolWindow();

    public ResourceToolWindow() {
        final Icon ng = IconLoader.getIcon("/images/ng.png");


    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true);
        panel.setContent(contentPanel.getMainPanel());
        panel.setBackground(UIUtil.getFieldForegroundColor());

        //contentPanel.getJPanelLeft().setViewportView(tree);
        //contentPanel.getJPanelMiddle().setViewportView(tree);
        //contentPanel.getJPanelRight().setViewportView(tree);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

        //TODO check com.intellij.openapi.ui.ThreeComponentsSplitter and its usage
    }

    @Override
    public void init(ToolWindow window) {

    }

}
