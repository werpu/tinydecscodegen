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
