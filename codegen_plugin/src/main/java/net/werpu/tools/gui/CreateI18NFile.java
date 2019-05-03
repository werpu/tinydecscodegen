package net.werpu.tools.gui;

import lombok.Getter;

import javax.swing.*;

@Getter
public class CreateI18NFile {
    private JPanel rootPanel;
    private JRadioButton rbJson;
    private JRadioButton rbBoth;
    private JRadioButton rbTypescript;
    private JTextField txtFilename;
}
