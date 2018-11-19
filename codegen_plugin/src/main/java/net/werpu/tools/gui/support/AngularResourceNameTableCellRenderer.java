package net.werpu.tools.gui.support;

import net.werpu.tools.supportive.fs.common.IAngularFileContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Specialized Angular Resource Renderer
 */
public class AngularResourceNameTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (value instanceof IAngularFileContext) {
            IAngularFileContext mappedValue = (IAngularFileContext) value;
            String name = mappedValue.getDisplayName();
            Icon icon = mappedValue.getIcon();
            super.setIcon(icon);
            String value1 = name.replaceAll("\\s*\\[.*\\]", "");
            super.setValue(value1);
            super.setToolTipText(value1);
            return;
        }

        super.setValue(value);

    }

}
