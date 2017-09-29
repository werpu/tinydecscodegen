package gui;

import jdk.nashorn.internal.objects.annotations.Getter;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class Confirm extends JDialog {
    public static final String CURRENT = " (Current)";
    public static final String LAST_PARENT = " (Last Parent)";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBox1;

    private DataRunnable performOk;

    private Runnable performCancel;

    private List<String> comboBoxElements;

    private String selectedClass;

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
        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                selectedClass = (String)cb.getSelectedItem();

            }
        });
    }

    public Confirm(DataRunnable<String> performOk, Runnable performCancel, List<String> comboBoxElements) {
        this();
        this.performOk = performOk;
        this.performCancel = performCancel;
        this.comboBoxElements = comboBoxElements;
        comboBoxElements.set(0 ,comboBoxElements.get(0)+ CURRENT);

        comboBoxElements.set(comboBoxElements.size() - 1 ,comboBoxElements.get(comboBoxElements.size() - 1)+ LAST_PARENT);
        comboBox1.setModel(new DefaultComboBoxModel<String>(comboBoxElements.toArray(new String[comboBoxElements.size()])));
    }

    private void onOK() {
        // add your code here
        dispose();
        if(performOk != null) {

            performOk.run(selectedClass.replace(CURRENT, "").replace(LAST_PARENT, ""));
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
