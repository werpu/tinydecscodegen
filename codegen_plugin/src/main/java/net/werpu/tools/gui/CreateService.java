package net.werpu.tools.gui;

import net.werpu.tools.gui.support.RegexpFormatter;
import net.werpu.tools.gui.support.RequiredListener;
import lombok.Getter;

import javax.swing.*;
import java.util.function.Function;

@Getter
public class CreateService {
    boolean selectorValid = false;
    Function<Boolean, Boolean> okFunc;
    Function<Boolean, Boolean> cancelFunc;
    private JLabel lblTitle;
    private JLabel lblSelector;
    private JFormattedTextField txtName;
    private JLabel lblExport;
    private JCheckBox cbExport;
    private JPanel mainPanel;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        txtName = new JFormattedTextField(new RegexpFormatter(".*"));
        new RequiredListener(txtName).addFormValidListener(submittable -> {
            selectorValid = submittable;
            return submittable;
        });
    }

    public void callOk() {
        if (okFunc != null) {
            okFunc.apply(true);
        }
    }

    public void callCancel() {
        if (cancelFunc != null) {
            cancelFunc.apply(true);
        }
    }
}
