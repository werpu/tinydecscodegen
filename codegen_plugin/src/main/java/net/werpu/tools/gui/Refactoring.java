package net.werpu.tools.gui;

import com.google.common.base.Strings;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

@Getter
public class Refactoring {
    private JLabel lblNewText;
    private JTextArea txtNewText;
    private JPanel rootPanel;
    private JScrollPane editorScroll;
    private JLabel lblCodeLevel;
    private JRadioButton rbTnDec;
    private JRadioButton rbNg;
    private JCheckBox cbBootstrap;


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


    public void onStartupModuleChange( Consumer<Boolean> startupListener) {
        cbBootstrap.addItemListener(e  -> startupListener.accept(e.getStateChange() == ItemEvent.SELECTED ));
    }
}
