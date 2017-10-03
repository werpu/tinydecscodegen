package actions;

import com.google.common.base.Strings;
import com.intellij.openapi.ui.ValidationInfo;

import javax.swing.*;

public class FormAssertions {

    public static final String TAG_SELECTOR_PATTERN = "[0-9a-z\\-]+";
    public static final String VALID_NAME = "[0-9a-zA-Z\\$\\_]+";
    public static final String MODULE_PATTERN = "[0-9A-Za-z\\.]+";

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
