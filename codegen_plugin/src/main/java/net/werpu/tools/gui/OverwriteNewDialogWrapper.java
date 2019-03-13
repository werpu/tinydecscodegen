package net.werpu.tools.gui;

import com.intellij.CommonBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * wrapper for the overwrite new dialog
 * so that we can reuse parts of it in intellij
 */
public class OverwriteNewDialogWrapper extends DialogWrapper {
    OverwriteNewDialog dialog;


    public OverwriteNewDialogWrapper(@Nullable Project project, OverwriteNewDialog dialog) {
        super(project, true);
        this.dialog = dialog;
        init();
    }


    @Override
    protected void init() {
        super.init();
        this.dialog.getBtnPanel().setVisible(false);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return dialog.getContentPanel();
    }

    @NotNull
    protected Action[] createActions() {
        return new Action[] {createOverwriteAction(), createNewAction(), createCancelAction()};
    }

    protected  DialogWrapperExitAction createOverwriteAction() {
        return new  DialogWrapperExitAction("Overwrite", 0) {
            @Override
            protected void doAction(ActionEvent e) {
                dialog.handleOverwrite(e);
                super.doAction(e);
            }
        };
    }

    protected  DialogWrapperExitAction createNewAction() {
        return new  DialogWrapperExitAction("New Entry", 0) {
            @Override
            protected void doAction(ActionEvent e) {
                dialog.handleNew(e);
                super.doAction(e);
            }
        };
    }

    protected  DialogWrapperExitAction createCancelAction() {
        return  new  DialogWrapperExitAction(CommonBundle.getCancelButtonText(), 1) {
            @Override
            protected void doAction(ActionEvent e) {
                doCancelAction();
            }
        };
    }
}
