/*

Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software
is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package net.werpu.tools.actions_all.shared;

import com.google.common.base.Strings;
import com.intellij.openapi.ui.ValidationInfo;

import javax.swing.*;

public class FormAssertions {

    public static final String TAG_SELECTOR_PATTERN = "[0-9a-z\\-]+";
    public static final String VALID_NAME = "[0-9a-zA-Z\\$\\_]+";
    public static final String VALID_ROUTE = "[0-9a-zA-Z\\$\\_\\.]+";
    public static final String MODULE_PATTERN = "[0-9A-Za-z\\.]+";

    public static ValidationInfo assertNotNullOrEmpty(String data, String message, JComponent parentComponent) {
        if (Strings.isNullOrEmpty(data)) {
            return (parentComponent == null) ? new ValidationInfo(message) : new ValidationInfo(message, parentComponent);
        }
        return null;
    }

    public static ValidationInfo assertTrue(boolean data, String message, JComponent parentComponent) {
        if (!data) {
            return (parentComponent == null) ? new ValidationInfo(message) : new ValidationInfo(message, parentComponent);
        }
        return null;
    }

    public static ValidationInfo assertPattern(String data, String pattern, String message, JComponent parentComponent) {
        data = Strings.nullToEmpty(data);
        if (!data.matches(pattern)) {
            return (parentComponent == null) ? new ValidationInfo(message) : new ValidationInfo(message, parentComponent);
        }
        return null;
    }

}
