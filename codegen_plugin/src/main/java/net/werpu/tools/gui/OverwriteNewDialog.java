package net.werpu.tools.gui;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class OverwriteNewDialog extends JDialog {
    private JPanel contentPane;
    private JButton btnOverwrite;
    private JButton btnCancel;
    private JButton btnNewEntry;

    Consumer<ActionEvent> overwriteHandler;
    Consumer<ActionEvent> newEntryHandler;

    @Getter
    boolean isOverwriteOutcome;
    @Getter
    boolean isNewEntryOutcome;


    public OverwriteNewDialog() {
        setContentPane(contentPane);
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
        contentPane.registerKeyboardAction(e -> onCancel(null), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        btnNewEntry.addActionListener(e -> {
            if (newEntryHandler != null) {
                newEntryHandler.accept(e);
            }
            this.isNewEntryOutcome = true;
            dispose();
        });
    }

    private void onOK(ActionEvent e) {
        // add your code here
        if (this.overwriteHandler != null) {
            this.overwriteHandler.accept(e);
        }
        this.isOverwriteOutcome = true;
        dispose();
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
