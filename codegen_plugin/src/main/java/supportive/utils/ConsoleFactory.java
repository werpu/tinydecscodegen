package supportive.utils;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.util.function.Consumer;

/**
 * a provider class which provides an output console on a long running task
 */
public class ConsoleFactory {

    public static ConsoleView getInstance(Process p, Project project, String consoleId) {
        OSProcessHandler handler = new OSProcessHandler(p, "Run npm install");

        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        String id = consoleId;
        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder builder = factory.createBuilder(project);
        ConsoleView view = builder.getConsole();
        view.setOutputPaused(false);

        handler.startNotify();
        view.attachToProcess(handler);

        ToolWindow window = manager.getToolWindow(id);


        if (window == null) {
            window = manager.registerToolWindow(id, true, ToolWindowAnchor.BOTTOM, view, true);

            final ContentFactory contentFactory = window.getContentManager().getFactory();
            final Content content = contentFactory.createContent(view.getComponent(), "NPM Install Console", true);
            window.setAutoHide(false);
            window.getContentManager().addContent(content);
            window.show(() -> {
            });

        }
        return view;
    }


    public static ConsoleView getInstance(Consumer<ConsoleView> m, Project project, String consoleId) {

        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        String id = consoleId;
        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder builder = factory.createBuilder(project);
        ConsoleView view = builder.getConsole();
        view.setOutputPaused(false);

        ToolWindow window = manager.getToolWindow(id);

        if (window == null) {
            window = manager.registerToolWindow(id, true, ToolWindowAnchor.BOTTOM, view, true);

            final ContentFactory contentFactory = window.getContentManager().getFactory();
            final Content content = contentFactory.createContent(view.getComponent(), "NPM Install Console", true);
            window.setAutoHide(false);
            window.getContentManager().addContent(content);
            window.show(() -> m.accept(view));

        }
        return view;
    }

}
