package gui.support;

import com.google.common.base.Strings;
import supportive.fs.common.IAngularFileContext;
import supportive.fs.common.NgModuleFileContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Specialized Angular Resource Renderer
 */
public class AngularResourceModuleTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if(value instanceof IAngularFileContext) {
            IAngularFileContext mappedValue = (IAngularFileContext) value;

            String displayName = mappedValue.getDisplayName();
            String name = displayName.substring(displayName.indexOf("[")+1, displayName.indexOf("]"));
            super.setValue(name);
            return;
        }

        super.setValue(value);

    }

}
