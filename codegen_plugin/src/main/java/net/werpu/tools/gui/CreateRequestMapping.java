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

import net.werpu.tools.supportive.utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CreateRequestMapping {

    private JTextField txtRestPath;
    private JRadioButton rbGet;
    private JRadioButton rbPost;
    private JRadioButton rbPut;
    private JRadioButton rbDelete;
    private JCheckBox cbTypeScript;

    private JCheckBox cbList;
    private JTextField txtMethodName;
    private JCheckBox cbCalcRest;
    private JPanel rootPanel;
    private JTextField txtReturnType;
    private JRadioButton rbTnDec;
    private JRadioButton rbAngNg;

    public CreateRequestMapping() {
        txtMethodName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }


        });
        cbCalcRest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
    }

    public void update() {
        if (!cbCalcRest.isSelected()) {
            return;
        }
        SwingUtils.update(txtRestPath, txtMethodName);
    }

    public JRadioButton getRbGet() {
        return rbGet;
    }

    public JRadioButton getRbPost() {
        return rbPost;
    }

    public JRadioButton getRbPut() {
        return rbPut;
    }

    public JRadioButton getRbDelete() {
        return rbDelete;
    }

    public JCheckBox getCbTypeScript() {
        return cbTypeScript;
    }


    public JCheckBox getCbList() {
        return cbList;
    }

    public JTextField getTxtRestPath() {
        return txtRestPath;
    }

    public JTextField getTxtMethodName() {
        return txtMethodName;
    }

    public JCheckBox getCbCalcRest() {
        return cbCalcRest;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTextField getTxtReturnType() {
        return txtReturnType;
    }

    public JRadioButton getRbTnDec() {
        return rbTnDec;
    }

    public JRadioButton getRbAngNg() {
        return rbAngNg;
    }
}
