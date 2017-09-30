package gui;

import com.google.common.base.Strings;
import dtos.ComponentJson;
import gui.support.RequiredListener;
import gui.support.RegexpFormatter;

import javax.swing.*;
import java.util.function.Function;


public class CreateTnDecComponent {
    private JFormattedTextField selector;
    private JTextArea template;
    private JButton cancelButton;
    private JButton okButton;
    private JTextField controllerAs;
    public JPanel rootPanel;

    boolean selectorValid = false;
    boolean controllerAsValid = false;

    Function<ComponentJson, Boolean> okFunc;
    Function<ComponentJson, Boolean> cancelFunc;

    public void onOk(Function<ComponentJson, Boolean> okFunc) {
        this.okFunc = okFunc;
    }

    public void onCancel(Function<ComponentJson, Boolean> cancelFunc) {
        this.cancelFunc = cancelFunc;
    }


    public CreateTnDecComponent() {
        okButton.addActionListener(e -> {

            if (okFunc != null)
                okFunc.apply(new ComponentJson(selector.getText(), Strings.nullToEmpty(template.getText()), controllerAs.getText()));

        });
        cancelButton.addActionListener(e -> {
            if (cancelFunc != null)
                cancelFunc.apply(new ComponentJson(selector.getText(), Strings.nullToEmpty(template.getText()), controllerAs.getText()));

        });
        selector.addPropertyChangeListener(evt -> {
            System.out.println(evt.getPropertyName());
        });
        template.addPropertyChangeListener(evt -> {

        });
        controllerAs.addPropertyChangeListener(evt -> {

        });
    }

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

    private void triggerButtonRecalc() {
        if(okButton != null) {
            okButton.setEnabled(selectorValid && controllerAsValid);
        }
    }

    public static void main(String[] args) {
        JDialog frame = new JDialog();

        frame.setContentPane(new CreateTnDecComponent().rootPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();

        frame.setVisible(true);
    }
}
