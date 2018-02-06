package gui;

import lombok.Getter;
import supportive.utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Getter
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

    public void update() {
        if (!cbCalcRest.isSelected()) {
            return;
        }
        SwingUtils.update(txtRestPath, txtServiceName);
    }


}
