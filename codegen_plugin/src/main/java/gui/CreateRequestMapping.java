package gui;

import javax.swing.*;

public class CreateRequestMapping {
    private JTextField textField1;
    private JRadioButton rbGet;
    private JRadioButton rbPost;
    private JRadioButton rbPut;
    private JRadioButton rbDelete;
    private JCheckBox rbTypeScript;
    private JComboBox cbReturnType;
    private JCheckBox cbList;


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

    public JCheckBox getRbTypeScript() {
        return rbTypeScript;
    }

    public JComboBox getCbReturnType() {
        return cbReturnType;
    }

    public JCheckBox getCbList() {
        return cbList;
    }
}
