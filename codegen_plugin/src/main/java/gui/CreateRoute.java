package gui;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import supportive.utils.StringUtils;
import supportive.utils.SwingUtils;

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

    public CreateRoute() {

        txtRouteName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateHref();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateHref();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateHref();
            }
        });

        cbSyncHref.addActionListener(e -> updateHref());

        cbComponent.addItemListener(e -> {
            updateRouteName(e);
        });
    }

    public void updateRouteName(ItemEvent e) {
        String oldValue = e != null ? (String) e.getItem() : "";
        String mappedName = StringUtils.toLowerDash(oldValue).replaceAll("_component", "");
        String newValue = StringUtils.toLowerDash((String) cbComponent.getSelectedItem()).replaceAll("_component", "");
        newValue = Strings.isNullOrEmpty(getTxtRouteName().getText())  ? newValue : getTxtRouteName().getText().replace(mappedName, newValue);
        getTxtRouteName().setText(newValue);
        updateHref();
    }

    public void updateHref() {
        if (!cbSyncHref.isSelected()) {
            return;
        }
        SwingUtils.update(txtHref, txtRouteName);
    }
}