package net.werpu.tools.gui.support;

import com.google.common.base.Strings;
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

            String displayName = Strings.nullToEmpty(mappedValue.getDisplayName()) ;
            String name = (displayName.contains("[") && displayName.contains("]")) ? displayName.substring(displayName.indexOf("[") + 1, displayName.indexOf("]")) : "";
            super.setValue(name);
            super.setToolTipText(name);
            return;
        }

        super.setValue(value);

    }

}
