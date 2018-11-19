package net.werpu.tools.gui.support;

import net.werpu.tools.supportive.fs.common.IAngularFileContext;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Specialized Angular Resource Renderer
 */
public class AngularResourceModuleTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (value instanceof IAngularFileContext) {
            IAngularFileContext mappedValue = (IAngularFileContext) value;

            String displayName = mappedValue.getDisplayName();
            String name = displayName.substring(displayName.indexOf("[") + 1, displayName.indexOf("]"));
            super.setValue(name);
            super.setToolTipText(name);
            return;
        }

        super.setValue(value);

    }

}
