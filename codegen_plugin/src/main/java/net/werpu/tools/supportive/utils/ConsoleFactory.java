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

package net.werpu.tools.supportive.utils;

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
