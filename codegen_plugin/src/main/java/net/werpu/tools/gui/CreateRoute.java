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

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import net.werpu.tools.supportive.utils.StringUtils;
import net.werpu.tools.supportive.utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;

@Getter
@Setter
public class CreateRoute {
    private JTextField txtRouteName;
    private JTextField txtHref;
    private JCheckBox cbSyncHref;
    private JComboBox cbComponent;
    private JPanel rootPanel;
    private JComboBox cbRegisterIntoModule;
    private JLabel lblRegisterIntoModule;
    private JLabel lblNavigationType;
    private JRadioButton rbRootNavigation;
    private JRadioButton rbModuleNavigation;

    public CreateRoute() {

        txtRouteName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateHref();
                updateNavButtons();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateHref();
                updateNavButtons();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateHref();
                updateNavButtons();
            }
        });

        cbSyncHref.addActionListener(e -> updateHref());

        cbComponent.addItemListener(e -> {
            updateRouteName(e);
            updateNavButtons();
        });
        rbModuleNavigation.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                lblRegisterIntoModule.setVisible(true);
                cbRegisterIntoModule.setVisible(true);
            }
        });
        rbRootNavigation.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                lblRegisterIntoModule.setVisible(false);
                cbRegisterIntoModule.setVisible(false);
            }
        });

        cbRegisterIntoModule.setVisible(false);
        lblRegisterIntoModule.setVisible(false);
    }

    public void updateRouteName(ItemEvent e) {
        String oldValue = e != null ? (String) e.getItem() : "";
        String mappedName = StringUtils.toLowerDash(oldValue).replaceAll("_component", "");
        String newValue = StringUtils.toLowerDash((String) cbComponent.getSelectedItem()).replaceAll("(\\[.*\\])+", "").replaceAll("_component", "");
        newValue = Strings.isNullOrEmpty(getTxtRouteName().getText()) ? newValue : getTxtRouteName().getText().replace(mappedName, newValue);
        getTxtRouteName().setText(newValue);
        updateHref();
    }

    public void updateHref() {
        if (!cbSyncHref.isSelected()) {
            return;
        }
        SwingUtils.update(txtHref, txtRouteName);
    }

    public void updateNavButtons() {
        if (txtRouteName.getText().contains(".")) {
            rbRootNavigation.setSelected(false);
            rbModuleNavigation.setSelected(true);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
