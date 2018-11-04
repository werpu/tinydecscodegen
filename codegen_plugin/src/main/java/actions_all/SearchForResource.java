package actions_all;

import actions_all.shared.VisibleAssertions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.WindowManager;
import gui.ResourceSearch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import supportive.fs.common.*;
import supportive.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.*;
import static supportive.fs.common.AngularVersion.NG;
import static supportive.fs.common.AngularVersion.TN_DEC;
import static supportive.utils.IntellijRunUtils.invokeLater;
import static supportive.utils.IntellijRunUtils.smartInvokeLater;
import static supportive.utils.SwingUtils.addMouseClickedHandler;

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


        JOptionPane mainFraime = new JOptionPane();
        mainFraime.setLayout(new BorderLayout());
        ResourceSearch resourceSearchPanel = new ResourceSearch();
        mainFraime.add(resourceSearchPanel.getMainPanel(), BorderLayout.CENTER);
        JDialog dialog = mainFraime.createDialog(WindowManager.getInstance().getFrame(project.getProject()).getRootPane(), "Find Resource" );
        resourceSearchPanel.setupTable();


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
                    openEditor(project, dialog, tblResults);
                    return;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case VK_DOWN: {
                        int selRow = tblResults.getSelectedRow();
                        selRow++;
                        selRow = Math.max(0, Math.min(tblResults.getRowCount() - 1, selRow));
                        tblResults.setRowSelectionInterval(selRow, selRow);
                        return;
                    }
                    case VK_UP: {
                        int selRow = tblResults.getSelectedRow();
                        selRow--;
                        selRow = Math.max(0, selRow);
                        tblResults.setRowSelectionInterval(selRow, selRow);
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
        });


        tblResults.addMouseListener(addMouseClickedHandler((ev) -> {
        }, (ev) -> {
            ev.consume();
            openEditor(project, dialog, tblResults);
        }));

        searchRefresh(resourceSearchPanel, project, fileContext);

        dialog.setResizable(true);
        dialog.pack();

        invokeLater(() -> {


            resourceSearchPanel.getTxtSearch().getTextArea().setFocusable(true);
            resourceSearchPanel.getTxtSearch().getTextArea().requestFocusInWindow();
        });
        dialog.setVisible(true);


    }

    private void openEditor(IntellijFileContext project, JDialog dialog, JTable tblResults) {
        int row = tblResults.getSelectedRow();
        if (row == -1) {
            return;
        }
        IAngularFileContext ctx = (IAngularFileContext) tblResults.getModel().getValueAt(row, 0);

        SwingUtils.openEditor(new IntellijFileContext(project.getProject(), ctx.getPsiFile()));
        dialog.setVisible(false);
        dialog.dispose();
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
        return true;
    }
}
