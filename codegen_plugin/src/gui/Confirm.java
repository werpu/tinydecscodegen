package gui;

import jdk.nashorn.internal.objects.annotations.Getter;

import javax.swing.*;
import java.awt.event.*;

public class Confirm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private Runnable performOk;

    private Runnable performCancel;

    public Confirm() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public Confirm(Runnable performOk, Runnable performCancel) {
        this();
        this.performOk = performOk;
        this.performCancel = performCancel;
    }

    private void onOK() {
        // add your code here
        dispose();
        if(performOk != null) {
            performOk.run();
        }

    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        if(performCancel != null) {
            performCancel.run();
        }

    }

    public static void main(String[] args) {
        Confirm dialog = new Confirm();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
