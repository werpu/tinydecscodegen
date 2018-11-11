package net.werpu.tools.gui;

import com.google.common.base.Strings;
import lombok.Getter;

import javax.swing.*;

@Getter
public class Refactoring {
    private JLabel lblTitle;
    private JLabel lblTitleValue;
    private JLabel lblNewText;
    private JTextArea txtNewText;
    private JPanel rootPanel;
    private JScrollPane editorScroll;


    public String getNewText() {
        return Strings.nullToEmpty(txtNewText.getText());
    }
}
