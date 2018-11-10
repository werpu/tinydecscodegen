package net.werpu.tools.gui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

@Getter
@Setter
public class ConfigServiceGen {
    private JSpinner returnValueLevel;
    private JComboBox targetFramework;
    private JLabel lblRestSourceFramework;
    private JComboBox sourceFramework;
    private JLabel lblStripReturnLevel;
    private JPanel rootPanel;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        SpinnerModel sm = new SpinnerNumberModel(0, 0, 10, 1); //default value,lower bound,upper bound,increment by
        returnValueLevel = new JSpinner(sm);
    }
}
