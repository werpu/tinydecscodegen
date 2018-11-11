package net.werpu.tools.gui;

import com.google.common.base.Strings;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;

@Getter
public class Refactoring {
    private JLabel lblTitle;
    private JLabel lblTitleValue;
    private JLabel lblNewText;
    private JTextArea txtNewText;
    private JPanel rootPanel;
    private JScrollPane editorScroll;
    private JLabel lblCodeLevel;
    private JRadioButton rbTnDec;
    private JRadioButton rbNg;


    public String getNewText() {
        return Strings.nullToEmpty(txtNewText.getText());
    }

    /**
     * selection listeners to handle real time text display
     */
    public void onbNgSelected( Runnable r) {
        rbNg.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                r.run();
            }
        });
    }

    public void onTnDecSelected( Runnable r) {
        rbTnDec.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                r.run();
            }
        });
    }
}
