package net.werpu.tools.gui;

import com.intellij.openapi.ui.DialogBuilder;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

@Getter
public class OverwriteNewDialog extends JDialog {
    private JPanel contentPanel;
    private JButton btnOverwrite;
    private JButton btnCancel;
    private JButton btnNewEntry;
    private JPanel mainForm;
    private JPanel btnPanel;

    Consumer<ActionEvent> overwriteHandler;
    Consumer<ActionEvent> newEntryHandler;


    boolean isOverwriteOutcome;
    boolean isNewEntryOutcome;
    DialogBuilder builder;

    public OverwriteNewDialog() {
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(btnOverwrite);

        btnOverwrite.addActionListener(e -> onOK(e));

        btnCancel.addActionListener(e -> onCancel(e));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel(null);
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(e -> onCancel(null), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        btnNewEntry.addActionListener(e -> {
            handleNew(e);
            dispose();
        });
    }

    public void handleNew(ActionEvent e) {
        if (newEntryHandler != null) {
            newEntryHandler.accept(e);
        }
        this.isNewEntryOutcome = true;
    }

    private void onOK(ActionEvent e) {
        handleOverwrite(e);
        dispose();
    }

    public void handleOverwrite(ActionEvent e) {
        // add your code here
        if (this.overwriteHandler != null) {
            this.overwriteHandler.accept(e);
        }
        this.isOverwriteOutcome = true;
    }


    @Override
    public void dispose() {
        if(builder != null) {
            builder.dispose();
        } else {
            super.dispose();
        }

    }

    private void onCancel(ActionEvent e) {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        OverwriteNewDialog dialog = new OverwriteNewDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
