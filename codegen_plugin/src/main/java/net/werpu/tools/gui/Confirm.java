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

package net.werpu.tools.gui;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Function;

@Getter
public class Confirm extends JDialog {
    public static final String CURRENT = " (Current)";
    public static final String LAST_PARENT = " (Last Parent)";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBox1;

    private Function<String, Boolean> performOk;

    private Runnable performCancel;

    private List<String> comboBoxElements;

    private String selectedClass;

    public Confirm(Function<String, Boolean> performOk, Runnable performCancel, List<String> comboBoxElements) {
        this();
        this.setTitle("Inheritance Hierarchy Detected");

        this.performOk = performOk;
        this.performCancel = performCancel;
        this.comboBoxElements = comboBoxElements;
        comboBoxElements.set(0, comboBoxElements.get(0) + CURRENT);

        this.selectedClass = comboBoxElements.get(0);
        comboBoxElements.set(comboBoxElements.size() - 1, comboBoxElements.get(comboBoxElements.size() - 1) + LAST_PARENT);
        comboBox1.setModel(new DefaultComboBoxModel<String>(comboBoxElements.toArray(new String[comboBoxElements.size()])));
    }


    public Confirm() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        comboBox1.addActionListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            selectedClass = (String) cb.getSelectedItem();
        });
    }


    private void onOK() {
        // add your code here
        dispose();
        if (performOk != null) {
            this.performOk.apply(selectedClass.replace(CURRENT, "").replace(LAST_PARENT, ""));
        }

    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        if (performCancel != null) {
            performCancel.run();
        }

    }


}
