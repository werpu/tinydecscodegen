package gui.support;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.intellij.openapi.ui.DialogWrapper.IdeModalityType.PROJECT;

/**
 * Builder for our standardized input dialog wrapper
 */
public class InputDialogWrapperBuilder {

    Project project;
    JPanel mainPanel;
    String dimensionKey = Math.random() + "";
    String dlgTitle;

    Dimension dlgPreferredSize;

    DialogWrapper.IdeModalityType modalityType = PROJECT;
    boolean canBeParent = true;
    boolean dlgModal = true;

    /**
     * we also drag the ok and cancel into our builder
     */
    BooleanSupplier okHandler = () -> true;
    BooleanSupplier cancelHandler = () -> true;

    Supplier<List<ValidationInfo>> validator = new Supplier() {
        @Override
        public Object get() {
            return Collections.emptyList();
        }
    };

    public InputDialogWrapperBuilder(Project project, JPanel mainPanel) {
        this.project = project;
        this.mainPanel = mainPanel;
    }

    public InputDialogWrapperBuilder withProject(Project project) {
        this.project = project;
        return this;
    }

    public InputDialogWrapperBuilder withMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
        return this;
    }

    public InputDialogWrapperBuilder withDimensionKey(String dimensionKey) {
        this.dimensionKey = dimensionKey;
        return this;
    }

    public InputDialogWrapperBuilder withModalityType(DialogWrapper.IdeModalityType modalityType) {
        this.modalityType = modalityType;
        return this;
    }

    public InputDialogWrapperBuilder withCanBeParent(boolean canBeParent) {
        this.canBeParent = canBeParent;
        return this;
    }


    public InputDialogWrapperBuilder withValidator(Supplier<List<ValidationInfo>> validator) {
        this.validator = validator;
        return this;
    }

    public InputDialogWrapperBuilder withTitle(String title) {
        this.dlgTitle = title;
        return this;
    }

    public InputDialogWrapperBuilder withPreferredSize(Dimension preferredSize) {
        this.dlgPreferredSize = preferredSize;
        return this;
    }

    public InputDialogWrapperBuilder withModal(boolean modal) {
        this.dlgModal = modal;
        return this;
    }

    public InputDialogWrapperBuilder withOkHandler(BooleanSupplier ok) {
        this.okHandler = ok;
        return this;
    }

    public InputDialogWrapperBuilder withCancelHandler(BooleanSupplier cancel) {
        this.cancelHandler = cancel;
        return this;
    }

    public DialogWrapper create() {
        return wrap(project, mainPanel, dimensionKey, validator, modalityType, canBeParent);
    }


    DialogWrapper wrap(Project project, JPanel mainPanel, String dimensionKey, Supplier<List<ValidationInfo>> validator, DialogWrapper.IdeModalityType modalityType, boolean canBeParent) {
        return new DialogWrapper(project, canBeParent, modalityType) {

            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                return mainPanel;
            }

            @Nullable
            @Override
            protected String getDimensionServiceKey() {
                return dimensionKey;
            }


            @Nullable
            @NotNull
            protected List<ValidationInfo> doValidateAll() {
                return validator.get();
            }


            @Override
            protected void doOKAction() {
                if (okHandler != null && okHandler.getAsBoolean()) {
                    super.doOKAction();
                } else if (okHandler == null) {
                    super.doOKAction();
                }
            }


            @Override
            public void doCancelAction() {
                if (cancelHandler != null && cancelHandler.getAsBoolean()) {
                    super.doCancelAction();
                } else if (cancelHandler == null) {
                    super.doCancelAction();
                }
            }

            @Override
            public void init() {
                super.init();
            }

            public void show() {
                if (dlgTitle != null) {
                    this.setTitle(dlgTitle);
                }

                if (dlgPreferredSize != null) {
                    this.getWindow().setPreferredSize(dlgPreferredSize);
                }
                this.init();
                this.setModal(dlgModal);
                this.pack();
                super.show();

            }
        };
    }
}
