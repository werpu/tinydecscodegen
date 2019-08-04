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

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

@Getter
public class Refactoring {
    private JLabel lblNewText;
    private JTextArea txtNewText;
    private JPanel rootPanel;
    private JScrollPane editorScroll;
    private JLabel lblCodeLevel;
    private JRadioButton rbTnDec;
    private JRadioButton rbNg;
    private JCheckBox cbBootstrap;


    public String getNewText() {
        return Strings.nullToEmpty(txtNewText.getText());
    }

    /**
     * selection listeners to handle real time text display
     */
    public void onbNgSelected( Runnable r) {
        rbNg.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                r.run();
            }
        });
    }

    public void onTnDecSelected( Runnable r) {
        rbTnDec.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                r.run();
            }
        });
    }


    public void onStartupModuleChange( Consumer<Boolean> startupListener) {
        cbBootstrap.addItemListener(e  -> startupListener.accept(e.getStateChange() == ItemEvent.SELECTED ));
    }
}
