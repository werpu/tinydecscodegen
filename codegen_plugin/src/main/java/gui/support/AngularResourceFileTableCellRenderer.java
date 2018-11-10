package gui.support;

import supportive.fs.common.IAngularFileContext;

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

            super.setValue(relativize.toString());
            return;
        }
        super.setValue(value);
    }
}
