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
import gui.support.RegexpFormatter;
import gui.support.RequiredListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

/**
 * A simple named artifact
 */
public class CreateTnNamedArtifact {
    private JFormattedTextField selector;

    private JButton cancelButton;
    private JButton okButton;
    private JTextField controllerAs;
    public JPanel rootPanel;
    private JLabel titlePane;
    private JButton btnEditTemplate;
    private JScrollPane textAreaPane;
    private JTextArea template;

    boolean selectorValid = false;
    boolean controllerAsValid = false;

    Function<ComponentJson, Boolean> okFunc;
    Function<ComponentJson, Boolean> cancelFunc;


    String textAreaInvisibleText = "Click to edit the template";
    String textAreaVisibileText = "Click to disable the template editing";


    public CreateTnNamedArtifact(String title) {

        titlePane.setText(title);
        okButton.addActionListener(e -> {
            if (okFunc != null)
                okFunc.apply(new ComponentJson(selector.getText(), Strings.nullToEmpty(""), controllerAs.getText()));

        });
        cancelButton.addActionListener(e -> {
            if (cancelFunc != null)
                cancelFunc.apply(new ComponentJson(selector.getText(), Strings.nullToEmpty(""), controllerAs.getText()));

        });
        selector.addPropertyChangeListener(evt -> {
            System.out.println(evt.getPropertyName());
        });


        btnEditTemplate.setBorderPainted(false);


        btnEditTemplate.addActionListener(e -> {
            textAreaPane.setVisible(!textAreaPane.isVisible());

            if(template.isVisible()) {
                btnEditTemplate.setText(textAreaVisibileText);

            } else {
                btnEditTemplate.setText(textAreaInvisibleText);

            }
        });

        textAreaPane.setVisible(false);
    }

    /**
     * We have some special behavior like required fields and formatted inputs
     */
    private void createUIComponents() {
        selector = new JFormattedTextField(new RegexpFormatter("[a-z0-9\\$\\-\\_]+"));
        new RequiredListener(selector).addFormValidListener(submittable -> {
            selectorValid = submittable;
            triggerButtonRecalc();
            return submittable;
        });
        controllerAs = new JTextField();
        new RequiredListener(controllerAs).addFormValidListener(submittable -> {
            controllerAsValid = submittable;
            triggerButtonRecalc();
            return submittable;
        });
        controllerAs.setText("ctrl");

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


    public void setTitle(String title) {
        this.titlePane.setText(title);
    }

    /**
     * a helper main for debugging purposes
     *
     * @param args
     */
    public static void main(String[] args) {
        JDialog frame = new JDialog();

        frame.setContentPane(new CreateTnNamedArtifact("Booga Artifact").rootPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();

        frame.setVisible(true);
    }
}
