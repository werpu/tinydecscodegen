package actions;

import com.google.common.base.Strings;
import com.intellij.openapi.ui.ValidationInfo;

import javax.swing.*;

public class FormAssertions {

    public static ValidationInfo assertNotNullOrEmpty(String data, String message, JComponent parentComponent) {
        if (Strings.isNullOrEmpty(data)) {
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
