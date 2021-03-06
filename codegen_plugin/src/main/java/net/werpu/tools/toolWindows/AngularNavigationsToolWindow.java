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

import com.google.common.base.Strings;
import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
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
import net.werpu.tools.supportive.fs.ng.NG_UIRoutesRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNAngularRoutesFileContext;
import net.werpu.tools.supportive.fs.tn.TNUIRoutesFileContext;
import net.werpu.tools.supportive.utils.IntellijRunUtils;
import net.werpu.tools.supportive.utils.SearchableTree;
import net.werpu.tools.toolWindows.supportive.ContextNodeRenderer;
import net.werpu.tools.toolWindows.supportive.MouseController;
import net.werpu.tools.toolWindows.supportive.PopupBuilder;
import net.werpu.tools.toolWindows.supportive.SwingRootParentNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static com.intellij.util.ui.tree.TreeUtil.expandAll;
import static net.werpu.tools.actions_all.shared.Labels.*;
import static net.werpu.tools.actions_all.shared.Messages.*;
import static net.werpu.tools.supportive.fs.common.AngularVersion.TN_DEC;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.NOOP_CONSUMER;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.smartInvokeLater;
import static net.werpu.tools.supportive.utils.IntellijUtils.convertToSearchableString;
import static net.werpu.tools.supportive.utils.IntellijUtils.getTsExtension;
import static net.werpu.tools.supportive.utils.StringUtils.normalizePath;
import static net.werpu.tools.supportive.utils.SwingUtils.copyToClipboard;
import static net.werpu.tools.supportive.utils.SwingUtils.openEditor;
import static net.werpu.tools.toolWindows.supportive.SwingRouteTreeFactory.createRouteTrees;

@CustomLog
public class AngularNavigationsToolWindow implements ToolWindowFactory {
    private SearchableTree<PsiRouteContext> routes = new SearchableTree();
    private IntellijFileContext projectRoot = null;
    private TreeSpeedSearch searchPath = null;
    private SimpleToolWindowPanel toolWindowPanel = null;

    public AngularNavigationsToolWindow() {

        routes.getTree().setCellRenderer(new ContextNodeRenderer());
        routes.getTree().setModel(new DefaultTreeModel(new DefaultMutableTreeNode(MSG_PLEASE_WAIT)));

        routes.createDefaultClickHandlers(NOOP_CONSUMER, this::goToComponent);
        routes.createDefaultKeyController(this::goToComponent, this::goToRouteDcl, this::copyRouteLink);

    }

    private void showPopup(PsiRouteContext foundContext, MouseEvent ev) {

        PopupBuilder builder = new PopupBuilder();
        builder.withMenuItem(LBL_GO_TO_COMPONENT, actionEvent -> goToComponent(foundContext))
                .withMenuItem(LBL_GO_TO_ROUTE_DECLARATION, actionEvent -> goToRouteDcl(foundContext))
                .withSeparator()
                .withMenuItem(LBL_COPY_ROUTE_LINK, actionEvent -> copyRouteLink(foundContext))
                .withMenuItem(LBL_COPY_ROUTE_KEY, actionEvent -> copyRouteName(foundContext))
                .show(routes.getTree(), ev.getX(), ev.getY());
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        IntellijRunUtils.onFileChange(project, (vFile) -> refreshContent(project, vFile));

        toolWindowPanel = new SimpleToolWindowPanel(true, true);

        refreshContent(project);
        JBScrollPane mainPanel = new JBScrollPane();
        toolWindowPanel.setContent(mainPanel);
        toolWindowPanel.setBackground(UIUtil.getFieldForegroundColor());

        mainPanel.setViewportView(routes.getTree());

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        if (toolWindow instanceof ToolWindowEx) {
            AnAction[] titleActions = new AnAction[]{
                    CommonActionsManager.getInstance().createExpandAllHeaderAction(routes.getTree()),
                    CommonActionsManager.getInstance().createCollapseAllHeaderAction(routes.getTree())
            };
            ((ToolWindowEx) toolWindow).setTitleActions(titleActions);
        }
    }

    private void refreshContent(@NotNull Project project, VirtualFile file) {
        IntellijFileContext vFileContext = new IntellijFileContext(project, file);

        boolean routeFileAffected = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot).stream()
                .anyMatch(routeFile -> routeFile.equals(vFileContext));

        ContextFactory.getInstance(projectRoot).getProjectResources(projectRoot, TN_DEC);

        if (routeFileAffected) {
            refreshContent(projectRoot.getProject());
            return;
        }

        //ModuleIndex allModules = ModuleIndex.getAllAffectedFiles(, )
        routeFileAffected = ContextFactory.getInstance(projectRoot)
                .getRouteFiles(projectRoot).stream()
                .anyMatch(routeFile -> routeFile.equals(vFileContext));

        if (routeFileAffected) {
            refreshContent(project);
        }
    }

    private void refreshContent(@NotNull Project project) {
        smartInvokeLater(project, () -> {
            try {
                try {
                    projectRoot = new IntellijFileContext(project);
                } catch (RuntimeException ex) {
                    //TODO logging here, the project was not resolvable
                    routes.getTree().setModel(new DefaultTreeModel(new DefaultMutableTreeNode(NO_PROJ_LATER)));
                    return;
                }

                List<IUIRoutesRoutesFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot);
                if (routeFiles == null || routeFiles.isEmpty()) {
                    routes.getTree().setModel(new DefaultTreeModel(new DefaultMutableTreeNode(MSG_NO_ROUTE_FOUND)));
                    return;
                }

                SwingRootParentNode routesHolder = new SwingRootParentNode(LBL_ROUTES);

                DefaultTreeModel newModel = new DefaultTreeModel(routesHolder);
                buildRoutesTree(routesHolder);

                routes.getTree().setRootVisible(false);
                routes.getTree().setModel(newModel);

                //now we restore the expansion state



                /*found this usefule helper in the jetbrains intellij sources*/
                if (searchPath == null) {

                    MouseController<PsiRouteContext> contextMenuListener = new MouseController<>(routes.getTree(), this::showPopup);
                    routes.getTree().addMouseListener(contextMenuListener);

                    searchPath = new TreeSpeedSearch(routes.getTree(), convertToSearchableString(routes.getTree()));
                }

                expandAll(routes.getTree());

            } catch (IndexNotReadyException exception) {
                refreshContent(project);
            }
        });
    }

    private void buildRoutesTree(SwingRootParentNode routesHolder) {
        List<IUIRoutesRoutesFileContext> routeFiles = ContextFactory.getInstance(projectRoot).getRouteFiles(projectRoot);

        routeFiles.forEach(ctx -> {

            String node = ctx instanceof NG_UIRoutesRoutesFileContext ? LBL_ANGLUAR_NG_ROUTES :
                    ctx instanceof TNAngularRoutesFileContext ? LBL_TN_DEC_ROUTES :
                            LBL_TN_DEC_UI_ROUTES;
            DefaultMutableTreeNode routes = createRouteTrees(ctx, node);

            routesHolder.add(routes);
        });
    }

    private void goToRouteDcl(PsiRouteContext foundContext) {
        openEditor(foundContext);
    }

    private void goToComponent(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (Strings.isNullOrEmpty(route.getComponentPath())) {
            Messages.showErrorDialog(this.routes.getTree().getRootPane(),
                    MSG_NO_COMP_PRES_CHECK_ROUTE, ERR_OCCURRED);
            return;
        }
        Path componentPath = Paths.get(route.getComponentPath());
        Path parent = Paths.get(Objects.requireNonNull(foundContext.getElement().getContainingFile().getParent()).getVirtualFile().getPath());
        Path rel = parent.relativize(componentPath);
        VirtualFile virtualFile = foundContext.getElement().getContainingFile().getParent().getVirtualFile().findFileByRelativePath(normalizePath(rel.toString()) + getTsExtension());
        if (virtualFile != null) {
            openEditor(new IntellijFileContext(foundContext.getElement().getProject(), virtualFile));
        }

    }

    /**
     * copies the route name into the clipboard
     */
    private void copyRouteName(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (route == null) {
            return;
        }
        copyToClipboard(route.getRouteKey());
    }

    /**
     * copies the fully qualified route link into the clipboard
     * to be inserted somewhere in the code
     */
    private void copyRouteLink(PsiRouteContext foundContext) {
        Route route = foundContext.getRoute();
        if (route == null) {
            return;
        }
        StringSelection stringSelection;
        String sRef;
        String routeLink = "";
        if (route.getOriginContext().equals(TNUIRoutesFileContext.class)) {
            sRef = HREF_TN_UIROUTES;
            routeLink = String.format(sRef, route.getRouteKey(), route.getRouteVarName());
        } else if (route.getOriginContext().equals(TNAngularRoutesFileContext.class)) {
            sRef = HREF_TN;
            routeLink = String.format(sRef, route.getUrl(), route.getRouteKey());
        } else {
            sRef = HREF_NG_UI_ROUTES;
            routeLink = String.format(sRef, route.getRouteKey(), route.getRouteVarName());
        }

        copyToClipboard(routeLink);
    }

}
