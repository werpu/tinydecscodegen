package net.werpu.tools.gui.support;

import net.werpu.tools.supportive.fs.common.IAngularFileContext;

import javax.swing.table.DefaultTableCellRenderer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AngularResourceFileTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    protected void setValue(Object value) {
        if (value instanceof IAngularFileContext) {
            IAngularFileContext mappedValue = (IAngularFileContext) value;
            String projectPath = mappedValue.getPsiFile().getProject().getBaseDir().getPath();
            Path relativize = Paths.get(projectPath).relativize(Paths.get(mappedValue.getVirtualFile().getPath()));

            String value1 = relativize.toString();
            super.setValue(value1);
            super.setToolTipText(value1);
            return;
        }
        super.setValue(value);
    }
}
