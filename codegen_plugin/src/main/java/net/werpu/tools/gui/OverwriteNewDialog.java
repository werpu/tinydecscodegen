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

import com.intellij.openapi.ui.DialogBuilder;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

@Getter
public class OverwriteNewDialog extends JDialog {
    Consumer<ActionEvent> overwriteHandler;
    Consumer<ActionEvent> newEntryHandler;
    boolean isOverwriteOutcome;
    boolean isNewEntryOutcome;
    DialogBuilder builder;
    private JPanel contentPanel;
    private JButton btnOverwrite;
    private JButton btnCancel;
    private JButton btnNewEntry;
    private JPanel mainForm;
    private JPanel btnPanel;

    public OverwriteNewDialog() {
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(btnOverwrite);

        btnOverwrite.addActionListener(e -> onOK(e));

        btnCancel.addActionListener(e -> onCancel(e));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel(null);
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(e -> onCancel(null), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        btnNewEntry.addActionListener(e -> {
            handleNew(e);
            dispose();
        });
    }

    public static void main(String[] args) {
        OverwriteNewDialog dialog = new OverwriteNewDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void handleNew(ActionEvent e) {
        if (newEntryHandler != null) {
            newEntryHandler.accept(e);
        }
        this.isNewEntryOutcome = true;
    }

    private void onOK(ActionEvent e) {
        handleOverwrite(e);
        dispose();
    }

    public void handleOverwrite(ActionEvent e) {
        // add your code here
        if (this.overwriteHandler != null) {
            this.overwriteHandler.accept(e);
        }
        this.isOverwriteOutcome = true;
    }

    @Override
    public void dispose() {
        if (builder != null) {
            builder.dispose();
        } else {
            super.dispose();
        }

    }

    private void onCancel(ActionEvent e) {
        // add your code here if necessary
        dispose();
    }
}
