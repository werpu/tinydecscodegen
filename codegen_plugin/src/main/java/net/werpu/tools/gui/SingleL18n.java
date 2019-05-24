package net.werpu.tools.gui;

import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.gui.support.IntelliFileContextComboboxModelEntry;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * Single L18n case form
 *
 * The idea is to have a single i18n
 * editing for for cases where it is not really
 * clear on how to proceed forward
 */
@Getter
public class SingleL18n {
    private JPanel rootPanel;
    private JLabel lblL18nFile;
    private JComboBox cbL18NFile;
    private JLabel lblKey;
    private JComboBox cbL18nKey;
    private JLabel lblText;
    private JTextField txtText;
    private JLabel lblTitle;
    private JLabel lblFileSwitcher;
    private JRadioButton rbAll;
    private JRadioButton rbExists;
    private JButton btnLoadCreate;
    private JLabel lblFileName;


    private java.util.List<IntelliFileContextComboboxModelEntry> allFiles;

    private java.util.List<IntelliFileContextComboboxModelEntry> containingFiles;


    public SingleL18n() {


        rbAll.addActionListener(ev -> switchToAllFiles());
        rbExists.addActionListener(ev -> switchToContainingFiles());

    }

    public void switchToAllFiles() {
        cbL18NFile.setModel(new ListComboBoxModel<>(allFiles));
        rbAll.setSelected(true);
    }

    public void switchToContainingFiles() {
        if(containingFiles != null && !containingFiles.isEmpty()) {
            cbL18NFile.setModel(new ListComboBoxModel<>(containingFiles));
            rbExists.setSelected(true);
        }
    }

    private void valueChanged() {
        rbExists.setEnabled(containingFiles != null && !containingFiles.isEmpty());
    }


    public void setAllFiles(List<IntelliFileContextComboboxModelEntry> allFiles) {
        this.allFiles = allFiles;
        valueChanged();
    }

    public void setContainingFiles(List<IntelliFileContextComboboxModelEntry> containingFiles) {
        this.containingFiles = containingFiles;
        valueChanged();
    }

    public void addFileChangeListener(ItemListener listener) {
        cbL18NFile.addItemListener(listener);
    }
}
