/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.gui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.werpu.tools.gui.support.IntelliFileContextComboboxModelEntry;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * Single L18n case form
 * <p>
 * The idea is to have a single i18n
 * editing for for cases where it is not really
 * clear on how to proceed forward
 */
@Getter
public class SingleL18n {
    private static final String KEY_PREFIX = "SINGLEL18N_";
    static final String KEY_RB_ALL = KEY_PREFIX + "RB_ALL";
    static final String KEY_RB_TS = KEY_PREFIX + "RB_TS";
    static final String KEY_RB_JSON = KEY_PREFIX + "RB_JSON";
    static final String KEY_SELECTED_FILE = KEY_PREFIX + "CB_SEL_FILE";
    private static final String KEY_TXT_PREFIX = KEY_PREFIX + "TXT_PREFIX";
    private static final String KEY_CB_TS_INTERNAT = KEY_PREFIX + "CB_TS_INTERNAT";
    VisibilityRules visibilityRules;
    PropertiesComponent props;
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
    private JLabel lblTargets;
    private JRadioButton rbJSON;
    private JRadioButton rbTS;
    private JRadioButton rbBoth;
    private JTextField txtPrefix;
    private JCheckBox cbTsInternat;
    private JLabel lblPrefix;
    private JLabel lblTsInternat;
    private java.util.List<IntelliFileContextComboboxModelEntry> allFiles;
    private java.util.List<IntelliFileContextComboboxModelEntry> containingFiles;

    public SingleL18n(Project project) {

        rbAll.addActionListener(ev -> switchToAllFiles());
        rbExists.addActionListener(ev -> switchToContainingFiles());

        visibilityRules = new VisibilityRules(this);

        registerVisibilityEvents();

        //load the defaults
        props = PropertiesComponent.getInstance(project);

        restoreSettings();
        updateVisibility();
    }

    public void restoreSettings() {
        String rbAll = props.getValue(KEY_RB_ALL);
        String rbTs = props.getValue(KEY_RB_TS);
        String rbJson = props.getValue(KEY_RB_JSON);

        String prefix = props.getValue(KEY_TXT_PREFIX);
        String internat = props.getValue(KEY_CB_TS_INTERNAT);

        if (Boolean.TRUE.toString().equals(rbAll)) {
            this.rbBoth.setSelected(true);
        }
        if (Boolean.TRUE.toString().equals(rbTs)) {
            this.rbTS.setSelected(true);
        }
        if (Boolean.TRUE.toString().equals(rbJson)) {
            this.rbJSON.setSelected(true);
        }
        if (Boolean.TRUE.toString().equals(internat)) {
            this.cbTsInternat.setSelected(true);
        }
        txtPrefix.setText(prefix != null ? prefix : "i18n");

    }

    public void saveSettings() {
        props.setValue(KEY_RB_ALL, Boolean.toString(this.rbAll.isSelected()));
        props.setValue(KEY_RB_TS, Boolean.toString(this.rbTS.isSelected()));
        props.setValue(KEY_RB_JSON, Boolean.toString(this.rbJSON.isSelected()));
        props.setValue(KEY_TXT_PREFIX, txtPrefix.getText());
        props.setValue(KEY_CB_TS_INTERNAT, Boolean.toString(this.cbTsInternat.isSelected()));
    }

    public void registerVisibilityEvents() {

        rbJSON.addActionListener(this::stateChanged);
        rbTS.addActionListener(this::stateChanged);
        rbBoth.addActionListener(this::stateChanged);
        cbTsInternat.addActionListener(this::stateChanged);
        cbL18NFile.addItemListener(itemEvent -> {
            IntelliFileContextComboboxModelEntry selectedItem = (IntelliFileContextComboboxModelEntry) itemEvent.getItem();
            boolean isMultitype = selectedItem.getTsFile() != null && selectedItem.getJSONFile() != null;
            this.multiType(isMultitype);
        });
    }

    public void switchToAllFiles() {
        cbL18NFile.setModel(new ListComboBoxModel<>(allFiles));
        rbAll.setSelected(true);
    }

    public void switchToContainingFiles() {
        if (containingFiles != null && !containingFiles.isEmpty()) {
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

    /**
     * display the multi type elements
     * for further processing
     *
     * @param visible
     */
    public void multiType(boolean visible) {
        this.rbJSON.setVisible(visible);
        this.rbAll.setVisible(visible);
        this.rbBoth.setVisible(visible);
        this.lblTargets.setVisible(visible);
        stateChanged((ChangeEvent) null);

    }

    private void updateVisibility() {
        lblPrefix.setVisible(visibilityRules.isShowPrefixLine());
        txtPrefix.setVisible(visibilityRules.isShowPrefixLine());
        lblTsInternat.setVisible(visibilityRules.isShowTSInternationalisation());
        cbTsInternat.setVisible(visibilityRules.isShowTSInternationalisation());
    }

    public boolean isMultiType() {
        return rbJSON.isVisible();
    }

    public void addFileChangeListener(ItemListener listener) {
        cbL18NFile.addItemListener(listener);
    }

    private void stateChanged(ChangeEvent event) {
        this.updateVisibility();
    }

    private void stateChanged(ActionEvent event) {
        this.updateVisibility();
    }

    public boolean isTypescriptReplacement() {
        return visibilityRules.isTypescriptReplacement();
    }
}


//visibility rules
@AllArgsConstructor
class VisibilityRules {
    SingleL18n dataSource;

    boolean isShowTSInternationalisation() {
        //the checkbox is only shown if the multiple radio button is checked
        return dataSource.getRbBoth().isVisible() && dataSource.getRbBoth().isSelected();
    }

    boolean isShowPrefixLine() {
        return (isShowTSInternationalisation() && dataSource.getCbTsInternat().isSelected()) ||
                dataSource.getRbTS().isSelected();
    }

    boolean isTypescriptReplacement() {
        boolean isTsFile = dataSource.getRbTS().isVisible() && dataSource.getRbTS().isSelected();
        boolean isBoth = dataSource.getRbBoth().isVisible() && dataSource.getRbBoth().isSelected();
        return isTsFile || (isBoth && dataSource.getCbTsInternat().isSelected());
    }

}