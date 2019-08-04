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
import dtos.ComponentJson;
import lombok.Getter;
import net.werpu.tools.gui.support.RegexpFormatter;
import net.werpu.tools.gui.support.RequiredListener;

import javax.swing.*;
import java.util.function.Function;

import static reflector.TransclusionReflector.getPossibleTransclusionSlots;
import static reflector.TransclusionReflector.hasTransclude;

/**
 * Create Component data form,
 * for the new Tiny Decs Create component dialog
 */
@Getter
public class CreateTnDecComponent {
    public JPanel rootPanel;
    public JScrollPane pnEditorHolder;
    boolean selectorValid = false;
    boolean controllerAsValid = false;
    Function<ComponentJson, Boolean> okFunc;
    Function<ComponentJson, Boolean> cancelFunc;
    private JFormattedTextField txtName;
    private JTextArea txtTemplate;
    private JButton cancelButton;
    private JButton okButton;
    private JTextField txtControllerAs;
    private JLabel lblSelector;
    private JLabel lblTemplate;
    private JLabel lblControllerAs;
    private JLabel lblTitle;
    private JLabel lblExport;
    private JCheckBox cbExport;
    private JCheckBox cbCreateDir;
    private JCheckBox cbCreateStructure;
    private JLabel lblCreateDir;
    private JLabel lblCreateStructue;
    private JComponent txtTemplate2;

    public CreateTnDecComponent() {
    }

    public void callOk() {
        if (okFunc != null) {
            String templateText = txtTemplate.getText();
            okFunc.apply(new ComponentJson(txtName.getText(), Strings.nullToEmpty(templateText), txtControllerAs.getText(),
                    hasTransclude(templateText), getPossibleTransclusionSlots(templateText)));
        }
    }

    public void callCancel() {
        if (cancelFunc != null) {
            String templateText = txtTemplate.getText();
            cancelFunc.apply(new ComponentJson(txtName.getText(), Strings.nullToEmpty(templateText), txtControllerAs.getText(),
                    hasTransclude(templateText), getPossibleTransclusionSlots(templateText)));
        }
    }


    public String getName() {
        return txtName.getText();
    }

    public String getTemplate() {
        return Strings.nullToEmpty(txtTemplate.getText());
    }

    public String getControllerAs() {
        return txtControllerAs.getText();
    }


    /**
     * We have some special behavior like required fields and formatted inputs
     */
    private void createUIComponents() {
        txtName = new JFormattedTextField(new RegexpFormatter(".*"));
        new RequiredListener(txtName).addFormValidListener(submittable -> {
            selectorValid = submittable;
            return submittable;
        });
        txtControllerAs = new JTextField();
        new RequiredListener(txtControllerAs).addFormValidListener(submittable -> {
            controllerAsValid = submittable;
            return submittable;
        });
        txtControllerAs.setText("ctrl");


    }


}
