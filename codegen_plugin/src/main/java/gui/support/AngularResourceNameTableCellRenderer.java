package gui.support;

import supportive.fs.common.IAngularFileContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Specialized Angular Resource Renderer
 */
public class AngularResourceNameTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if(value instanceof IAngularFileContext) {
            IAngularFileContext mappedValue = (IAngularFileContext) value;
            String name = mappedValue.getDisplayName();
            Icon icon = mappedValue.getIcon();
            super.setIcon(icon);
            super.setValue(name.replaceAll("\\s*\\[.*\\]", ""));
            return;
        }

        super.setValue(value);

    }

}
