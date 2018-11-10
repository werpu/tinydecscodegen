package net.werpu.tools.gui.support;

import com.google.common.collect.Lists;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.function.Function;

import static java.awt.Color.RED;

public class RequiredListener implements DocumentListener {

    JTextComponent comp = null;
    Border defaultBorder = null;
    Border highlightBorder = BorderFactory.createLineBorder(RED);

    List<Function<Boolean, Boolean>> listeners = Lists.newLinkedList();
    String regexp;

    public RequiredListener(JTextComponent jtc) {
        comp = jtc;
        defaultBorder = comp.getBorder();
        // Adding this listener to a specified component:
        comp.getDocument().addDocumentListener(this);
        this.maybeHighlight();
    }

    public RequiredListener(JTextComponent comp, String regexp) {
        this(comp);
        this.regexp = regexp;
    }

    public void insertUpdate(DocumentEvent e) {
        maybeHighlight();
    }

    public void removeUpdate(DocumentEvent e) {
        maybeHighlight();
    }

    public void changedUpdate(DocumentEvent e) {
        maybeHighlight();
    }

    public void addFormValidListener(Function<Boolean, Boolean> func) {
        listeners.add(func);
    }

    private void callListeners(boolean valid) {
        listeners.stream().forEach(item -> item.apply(valid));
    }

    private void maybeHighlight() {

        /*if(!comp.isValid()) {
            comp.setBorder(highlightBorder);
            callListeners(false);
            return;
        }*/


        if (comp.getText().trim().length() > 0) {
            // if a field is nonempty, switch it to default look
            comp.setBorder(defaultBorder);
            callListeners(true);
        } else {
            // if a field is empty, highlight it
            comp.setBorder(highlightBorder);
            callListeners(false);
            // ... more actions
        }

    }
}