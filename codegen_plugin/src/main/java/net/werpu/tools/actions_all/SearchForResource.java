package net.werpu.tools.actions_all;

import com.intellij.openapi.ui.popup.*;
import net.werpu.tools.actions_all.shared.VisibleAssertions;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.WindowMoveListener;
import net.werpu.tools.gui.ResourceSearch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.supportive.fs.common.*;
import net.werpu.tools.supportive.utils.SwingUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.*;
import static net.werpu.tools.supportive.fs.common.AngularVersion.NG;
import static net.werpu.tools.supportive.fs.common.AngularVersion.TN_DEC;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.invokeLater;
import static net.werpu.tools.supportive.utils.IntellijRunUtils.smartInvokeLater;
import static net.werpu.tools.supportive.utils.SwingUtils.addMouseClickedHandler;
import static net.werpu.tools.supportive.utils.TimeoutWorker.setTimeout;

@Getter
@Setter
@AllArgsConstructor
class Wrapper<T> {
    T value;
}

public class SearchForResource extends AnAction {


    @Override
    public void update(AnActionEvent e) {


        super.update(e);
        if (!e.getPresentation().isEnabledAndVisible()) {
            return;
        }
        VisibleAssertions.hasAngularVersion(e, TN_DEC);
        if (e.getPresentation().isEnabledAndVisible()) {
            return;
        }
        VisibleAssertions.hasAngularVersion(e, NG);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        IntellijFileContext fileContext = new IntellijFileContext(e);


        IntellijFileContext project = new IntellijFileContext(fileContext.getProject());
        ResourceSearch resourceSearchPanel = new ResourceSearch();


        JComponent parentWindow = WindowManager.getInstance().getIdeFrame(e.getProject()).getComponent();
        JPanel mainPanel = resourceSearchPanel.getMainPanel();
        final ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, parentWindow);


        JBPopup popup = builder.setProject(e.getProject())
                .setMovable(true)
                .setResizable(true)
                .setMayBeParent(true)
                .setCancelOnClickOutside(true)

                .setDimensionServiceKey(project.getProject(), "SEARCH_FOR_ANG_RESOURCE", true)

                .setRequestFocus(true)
                .setCancelKeyEnabled(false)

                .setCancelCallback(() -> {
                    List<JBPopup> popups = JBPopupFactory.getInstance().getChildPopups(parentWindow);
                    if (!popups.isEmpty()) {
                        for (JBPopup thePopup : popups) {
                            thePopup.cancel();
                        }
                        return false;
                    }
                    return true;
                }).createPopup();


        //mainFraime.add(resourceSearchPanel.getRootPanel(), BorderLayout.CENTER);
        //JDialog dialog = mainFraime.createDialog(WindowManager.getInstance().getFrame(project.getProject()).getRootPane(), "Find Resource");
        resourceSearchPanel.setupTable();

        registerCloseAction(popup);
        WindowMoveListener windowListener = new WindowMoveListener(popup.getContent());
        mainPanel.addMouseListener(windowListener);
        mainPanel.addMouseMotionListener(windowListener);
        mainPanel.setPreferredSize(new Dimension(900, 600));


        resourceSearchPanel.getRbInProject().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));
        resourceSearchPanel.getRbInModule().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));
        resourceSearchPanel.getCbModules().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));
        resourceSearchPanel.getCbComponents().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));
        resourceSearchPanel.getCbControllers().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));
        resourceSearchPanel.getCbServices().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));
        resourceSearchPanel.getCbFilters().addActionListener((ev) -> searchRefresh(resourceSearchPanel, project, fileContext));

        final JTable tblResults = resourceSearchPanel.getTblResults();
        resourceSearchPanel.getTxtSearch().getTextArea().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {


            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == VK_ENTER) {
                    openEditor(project, popup, tblResults);
                    return;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case VK_DOWN: {
                        rowDown();
                        return;
                    }
                    case VK_UP: {
                        rowUp();
                        return;
                    }
                    case VK_LEFT: {
                        return;
                    }
                    case VK_RIGHT: {
                        return;
                    }
                }
                searchRefresh(resourceSearchPanel, project, fileContext);
            }

            private void rowUp() {
                int selRow = tblResults.getSelectedRow();
                selRow--;
                selRow = Math.max(0, selRow);
                tblResults.setRowSelectionInterval(selRow, selRow);
            }

            private void rowDown() {
                int selRow = tblResults.getSelectedRow();
                selRow++;
                selRow = Math.max(0, Math.min(tblResults.getRowCount() - 1, selRow));
                tblResults.setRowSelectionInterval(selRow, selRow);
            }
        });


        tblResults.addMouseListener(addMouseClickedHandler((ev) -> {
        }, (ev) -> {
            ev.consume();
            openEditor(project, popup, tblResults);
        }));

        searchRefresh(resourceSearchPanel, project, fileContext);

        // dialog.setResizable(true);


        invokeLater(() -> {
            resourceSearchPanel.getTxtSearch().getTextArea().setFocusable(true);
            resourceSearchPanel.getTxtSearch().getTextArea().requestFocusInWindow();
        });
        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);

        popup.getContent().addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                resourceSearchPanel.getTxtSearch().getTextArea().setFocusable(true);
                resourceSearchPanel.getTxtSearch().getTextArea().requestFocusInWindow();
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        popup.setRequestFocus(true);

        popup.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(@NotNull LightweightWindowEvent event) {
                resourceSearchPanel.getTxtSearch().getTextArea().setFocusable(true);
                resourceSearchPanel.getTxtSearch().getTextArea().requestFocus();
            }
        });
        invokeLater(() -> {
            //popup.getContent().getRootPane().setFocusable(true);
            //popup.getContent().getRootPane().requestFocus(true);
            //resourceSearchPanel.getTxtSearch().getTextArea().viewToModel(new Point(0,0));
        });
        //popup.getOwner().setFocusable(true);
        //popup.getOwner().requestFocus();
        popup.addListener(new JBPopupListener() {
            @Override
            public void beforeShown(@NotNull LightweightWindowEvent event) {
                resourceSearchPanel.getTxtSearch().getTextArea().setFocusable(true);
                resourceSearchPanel.getTxtSearch().getTextArea().requestFocus();
            }
        });

        resourceSearchPanel.getTxtSearch().getTextArea().setFocusable(true);

        popup.showCenteredInCurrentWindow(e.getProject());
        setTimeout(() -> {
            invokeLater(() -> {
                resourceSearchPanel.getTxtSearch().getTextArea().requestFocus();
            });
        }, 100);



    }

    private void registerCloseAction(JBPopup popup) {
        final AnAction escape = ActionManager.getInstance().getAction("EditorEscape");
        DumbAwareAction closeAction = new DumbAwareAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
                if (popup != null && popup.isVisible()) {
                    popup.cancel();
                }
            }
        };
        closeAction.registerCustomShortcutSet(escape == null ? CommonShortcuts.ESCAPE : escape.getShortcutSet(), popup.getContent(), popup);
    }

    private void openEditor(IntellijFileContext project, JBPopup popup, JTable tblResults) {
        int row = tblResults.getSelectedRow();
        if (row == -1) {
            return;
        }
        IAngularFileContext ctx = (IAngularFileContext) tblResults.getModel().getValueAt(row, 0);

        SwingUtils.openEditor(new IntellijFileContext(project.getProject(), ctx.getPsiFile()));
        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
    }

    private void searchRefresh(ResourceSearch resourceSearchPanel, IntellijFileContext project, IntellijFileContext targetFile) {
        String searchValue = resourceSearchPanel.getTxtSearch().getTextArea().getText();
        smartInvokeLater(project.getProject(), () -> {
            performSearch(resourceSearchPanel, project, targetFile, searchValue);

        });

    }

    private void performSearch(ResourceSearch target, IntellijFileContext project, IntellijFileContext targetFile, String searchValue) {
        ResourceFilesContext tnResources = ContextFactory.getInstance(project).getProjectResourcesCached(project, TN_DEC);
        ResourceFilesContext ngResources = ContextFactory.getInstance(project).getProjectResourcesCached(project, AngularVersion.NG);

        tnResources = tnResources.search(searchValue);
        ngResources = ngResources.search(searchValue);

        java.util.List<IAngularFileContext> results = new ArrayList<>(100);

        if (target.getCbModules().isSelected()) {
            results.addAll(tnResources.getModules());
            results.addAll(ngResources.getModules());
        }
        if (target.getCbComponents().isSelected()) {
            results.addAll(tnResources.getComponents());
            results.addAll(ngResources.getComponents());
        }
        if (target.getCbControllers().isSelected()) {
            results.addAll(tnResources.getControllers());
            results.addAll(ngResources.getControllers());
        }
        if (target.getCbServices().isSelected()) {
            results.addAll(tnResources.getServices());
            results.addAll(ngResources.getServices());
        }
        if (target.getCbFilters().isSelected()) {
            results.addAll(tnResources.getFiltersPipes());
            results.addAll(ngResources.getFiltersPipes());
        }


        final Wrapper<java.util.List<IAngularFileContext>> wrapper = new Wrapper<>(results);

        if (target.getRbInModule().isSelected()) {
            Optional<NgModuleFileContext> moduleFileContext = targetFile.getNearestModule();
            moduleFileContext.ifPresent(ctx -> {
                wrapper.setValue(wrapper.getValue().stream().filter(item -> item.getVirtualFile().getPath()
                        .contains(ctx.getFolderPath())).collect(Collectors.toList()));
            });
            //results = result.
        }
        results = wrapper.getValue();

        Vector updatable = new Vector(results.stream().map(e -> new Vector(Arrays.asList(e, e, e))).collect(Collectors.toList()));

        target.updateData(updatable);

    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
