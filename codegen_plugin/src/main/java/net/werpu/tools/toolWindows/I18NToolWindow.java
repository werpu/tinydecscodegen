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

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.FileContentQueue;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import lombok.CustomLog;
import net.werpu.tools.supportive.fs.common.*;
import net.werpu.tools.supportive.transformations.i18n.I18NEntry;
import net.werpu.tools.supportive.utils.IntellijRunUtils;
import net.werpu.tools.supportive.utils.SearchableTree;
import net.werpu.tools.toolWindows.supportive.MouseController;
import net.werpu.tools.toolWindows.supportive.SwingI18NTreeFactory;
import net.werpu.tools.toolWindows.supportive.SwingI18NTreeNode;
import net.werpu.tools.toolWindows.supportive.SwingRootParentNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;
import java.util.Optional;

import static com.intellij.util.ui.tree.TreeUtil.expandAll;
import static net.werpu.tools.actions_all.shared.Labels.LBL_ROUTES;
import static net.werpu.tools.actions_all.shared.Messages.*;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.NOOP_CONSUMER;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.smartInvokeLater;
import static net.werpu.tools.supportive.utils.IntellijUtils.convertToSearchableString;
import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;
import static net.werpu.tools.supportive.utils.SwingUtils.openEditor;

/**
 * A tool window providing a tree view on the 18n, including content change facilities
 *
 */
@CustomLog
public class I18NToolWindow implements ToolWindowFactory {

    private SearchableTree<I18NFileContext> files = new SearchableTree<>();
    private IntellijFileContext projectRoot = null;
    private TreeSpeedSearch searchPath = null;

    public I18NToolWindow() {

        files.getTree().setCellRenderer(new I18NFileContextNodeRenderer());
        files.getTree().setModel(new DefaultTreeModel(new DefaultMutableTreeNode(MSG_PLEASE_WAIT)));

        files.createDefaultNodeClickHandlers(NOOP_CONSUMER, this::gotToDeclaration);
        //files.createDefaultKeyController(this::goToComponent, this::goToRouteDcl, this::copyRouteLink);



    }


    public void gotToDeclaration(SwingI18NTreeNode node) {
        I18NFileContext parFile = node.getI18NFileContext();
        String key = node.getI18NElement().getFullKey();
        Optional<PsiElementContext> value = parFile.getValue(key, false);
        if(!value.isPresent()) {
            return;
        }

        openEditor(value.get());

    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        IntellijRunUtils.onFileChange(project, (vFile) -> refreshContent(project));
        this.projectRoot = new IntellijFileContext(project);

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(true, true);

        refreshContent(project);
        JBScrollPane mainPanel = new JBScrollPane();
        toolWindowPanel.setContent(mainPanel);
        toolWindowPanel.setBackground(UIUtil.getFieldForegroundColor());

        mainPanel.setViewportView(files.getTree());

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);


        if (toolWindow instanceof ToolWindowEx) {
            AnAction[] titleActions = new AnAction[]{
                    CommonActionsManager.getInstance().createExpandAllHeaderAction(files.getTree()),
                    CommonActionsManager.getInstance().createCollapseAllHeaderAction(files.getTree())
            };
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);
        }
    }

    private void refreshContent(@NotNull Project project) {
        smartInvokeLater(project, () -> {
            try {
                try {
                    projectRoot = new IntellijFileContext(project);
                } catch (RuntimeException ex) {
                    //TODO logging here, the project was not resolvable
                    files.getTree().setModel(new DefaultTreeModel(new DefaultMutableTreeNode(NO_PROJ_LATER)));
                    return;
                }


                List<I18NFileContext> i18nFiles = ContextFactory.getInstance(projectRoot).getI18NFiles(projectRoot);
                if (i18nFiles.isEmpty()) {
                    files.getTree().setModel(new DefaultTreeModel(new DefaultMutableTreeNode(MSG_NO_ROUTE_FOUND)));
                    return;
                }

                SwingRootParentNode routesHolder = new SwingRootParentNode(LBL_ROUTES);

                DefaultTreeModel newModel = new DefaultTreeModel(routesHolder);
                buildI18NTree(routesHolder);

                files.getTree().setRootVisible(false);
                files.getTree().setModel(newModel);

                //now we restore the expansion state



                /*found this usefule helper in the jetbrains intellij sources*/
                if (searchPath == null) {


                    //MouseController<PsiRouteContext> contextMenuListener = new MouseController<>(files.getTree(), this::showPopup);
                    //files.getTree().addMouseListener(contextMenuListener);


                    searchPath = new TreeSpeedSearch(files.getTree(), convertToSearchableString(files.getTree()));
                }

                expandAll(files.getTree());

            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }

    private void buildI18NTree(SwingRootParentNode routesHolder) {
        List<I18NFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getI18NFiles(projectRoot);
        routeFiles.forEach(ctx -> {
            DefaultMutableTreeNode routes = SwingI18NTreeFactory.createRouteTrees(ctx, ctx.getBaseName());
            routesHolder.add(routes);
        });
    }

    static class I18NFileContextNodeRenderer extends NodeRenderer {
        //TODO add a bunch of new icons here
        private final Icon ng = IconLoader.getIcon("/images/ng.png");

        @Nullable
        @Override
        protected ItemPresentation getPresentation(Object node) {
            ItemPresentation retVal = super.getPresentation(node);
            Icon icon = ng;

            if(node instanceof I18NFileContext) {
                I18NFileContext data = (I18NFileContext) node;
                icon = ((I18NFileContext) node).getIcon();
                String fileName = normalizePath(data.getVirtualFile().getName());
                return new PresentationData(fileName, fileName, icon, null);
            } else if (node instanceof I18NElement) {
                I18NElement data = (I18NElement) node;
                return new PresentationData(data.getKey(), data.getStringValue(), icon, null);
            }
            return retVal;
        }
    }
}
