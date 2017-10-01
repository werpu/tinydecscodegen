/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package gui;

import com.google.common.base.Strings;
import dtos.ComponentJson;
import gui.support.RequiredListener;
import gui.support.RegexpFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * Create Component data form,
 * for the new Tiny Decs Create component dialog
 */
public class CreateTnDecComponent {
    private JFormattedTextField txtName;
    private JTextArea template;
    private JButton cancelButton;
    private JButton okButton;
    private JTextField txtControllerAs;
    public JPanel rootPanel;

    boolean selectorValid = false;
    boolean controllerAsValid = false;

    Function<ComponentJson, Boolean> okFunc;
    Function<ComponentJson, Boolean> cancelFunc;


    public CreateTnDecComponent() {
    }

    public void callOk() {
        if (okFunc != null)
            okFunc.apply(new ComponentJson(txtName.getText(), Strings.nullToEmpty(template.getText()), txtControllerAs.getText()));
    }

    public void callCancel() {
        if (cancelFunc != null)
            cancelFunc.apply(new ComponentJson(txtName.getText(), Strings.nullToEmpty(template.getText()), txtControllerAs.getText()));
    }


    public String getName() {
        return txtName.getText();
    }

    public String getTemplate() {
        return Strings.nullToEmpty(template.getText());
    }

    public String getControllerAs() {
        return txtControllerAs.getText();
    }


    public JFormattedTextField getTxtName() {
        return txtName;
    }

    public JTextField getTxtControllerAs() {
        return txtControllerAs;
    }

    /**
     * We have some special behavior like required fields and formatted inputs
     */
    private void createUIComponents() {
        txtName = new JFormattedTextField(new RegexpFormatter("[a-z0-9\\$\\-\\_]+"));
        new RequiredListener(txtName).addFormValidListener(submittable -> {
            selectorValid = submittable;
            triggerButtonRecalc();
            return submittable;
        });
        txtControllerAs = new JTextField();
        new RequiredListener(txtControllerAs).addFormValidListener(submittable -> {
            controllerAsValid = submittable;
            triggerButtonRecalc();
            return submittable;
        });
        txtControllerAs.setText("ctrl");

    }

    /**
     * disables and enables the ok button depending on the state
     * of the inputs
     */
    private void triggerButtonRecalc() {
        /*if (okButton != null) {
            boolean enabledState = selectorValid && controllerAsValid;
            if(okButton.isEnabled() != enabledState) {
                okButton.setEnabled(enabledState);
            }
        }*/
    }


    public void onOk(Function<ComponentJson, Boolean> okFunc) {
        this.okFunc = okFunc;
    }

    public void onCancel(Function<ComponentJson, Boolean> cancelFunc) {
        this.cancelFunc = cancelFunc;
    }


    public void initDefault(Window window) {
        SwingUtilities.getRootPane(window).setDefaultButton(okButton);
    }



    /**
     * a helper main for debugging purposes
     *
     * @param args
     */
    public static void main(String[] args) {
        JDialog frame = new JDialog();

        frame.setContentPane(new CreateTnDecComponent().rootPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();

        frame.setVisible(true);
    }
}
