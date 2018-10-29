package toolWindows.supportive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PopupBuilder {

    JPopupMenu popupMenu = new JPopupMenu();

    public PopupBuilder withMenuItem(String title, ActionListener l) {
        JMenuItem go_to_component = new JMenuItem(title);
        go_to_component.addActionListener(l);
        popupMenu.add(go_to_component);
        return this;
    }

    public PopupBuilder withSeparator() {

        popupMenu.addSeparator();
        return this;
    }

    public void show(Component invoker, int x, int y) {
        popupMenu.show(invoker, x, y);
    }
}
