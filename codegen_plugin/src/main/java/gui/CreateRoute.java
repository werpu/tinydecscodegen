package gui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

@Getter
@Setter
public class CreateRoute {
    private JTextField txtRouteName;
    private JTextField textField1;
    private JCheckBox cbSyncHref;
    private JComboBox comboBox1;
    private JPanel rootPanel;

    public CreateRoute() {
    }
}
