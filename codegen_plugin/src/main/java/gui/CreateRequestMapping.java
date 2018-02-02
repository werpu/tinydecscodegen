package gui;

import utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateRequestMapping {
    private JTextField txtRestPath;
    private JRadioButton rbGet;
    private JRadioButton rbPost;
    private JRadioButton rbPut;
    private JRadioButton rbDelete;
    private JCheckBox cbTypeScript;
    private JComboBox cbReturnType;
    private JCheckBox cbList;
    private JTextField txtMethodName;
    private JCheckBox cbCalcRest;
    private JPanel rootPanel;

    public CreateRequestMapping() {
        txtMethodName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }


        });
        cbCalcRest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
    }

    public void update() {
        if (!cbCalcRest.isSelected()) {
            return;
        }
        SwingUtils.update(txtRestPath, txtMethodName);
    }

    public JRadioButton getRbGet() {
        return rbGet;
    }

    public JRadioButton getRbPost() {
        return rbPost;
    }

    public JRadioButton getRbPut() {
        return rbPut;
    }

    public JRadioButton getRbDelete() {
        return rbDelete;
    }

    public JCheckBox getCbTypeScript() {
        return cbTypeScript;
    }

    public JComboBox getCbReturnType() {
        return cbReturnType;
    }

    public JCheckBox getCbList() {
        return cbList;
    }

    public JTextField getTxtRestPath() {
        return txtRestPath;
    }

    public JTextField getTxtMethodName() {
        return txtMethodName;
    }

    public JCheckBox getCbCalcRest() {
        return cbCalcRest;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}
