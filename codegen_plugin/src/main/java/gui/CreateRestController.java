package gui;

import com.google.common.base.Strings;
import utils.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateRestController {
    private JTextField txtServiceName;
    private JTextField txtRestPath;
    private JPanel rootPanel;
    private JCheckBox cbCalcRest;
    private JCheckBox cbCreate;
    private JRadioButton cbTnDec;
    private JRadioButton cbNg;


    public CreateRestController() {

        txtServiceName.getDocument().addDocumentListener(new DocumentListener() {
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

    public JTextField getTxtServiceName() {
        return txtServiceName;
    }

    public JTextField getTxtRestPath() {
        return txtRestPath;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JCheckBox getCbCalcRest() {
        return cbCalcRest;
    }

    public JCheckBox getCbCreate() {
        return cbCreate;
    }

    public JRadioButton getCbTnDec() {
        return cbTnDec;
    }

    public JRadioButton getCbNg() {
        return cbNg;
    }

    protected void update() {
        if (!cbCalcRest.isSelected()) {
            return;
        }
        String txt = txtServiceName.getText();

        StringUtils.toLowerDash(txt);
        //Transform text

        String txtBase = Strings.nullToEmpty(txtRestPath.getText());
        String[] args = txtBase.split("\\/");
        String base = "";
        String name = "";

        if (args.length > 1) {
            base = txtBase.substring(0, txtBase.lastIndexOf("/"));
            //name = args[args.length - 1];
        } else if (args.length == 1) {
            //name = args[0];
        }
        name = StringUtils.toLowerDash(txt);


        txtRestPath.setText(base + "/" + name);
    }
}
