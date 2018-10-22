package gui.support;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.intellij.openapi.ui.DialogWrapper.IdeModalityType.PROJECT;

public class DialogWrapperCreator {

    Project project;
    JPanel mainPanel;
    String dimensionKey = Math.random()+"";

    DialogWrapper.IdeModalityType modalityType = PROJECT;
    boolean canBeParent = true;

    Supplier<List<ValidationInfo>> validator = new Supplier() {
        @Override
        public Object get() {
            return Collections.emptyList();
        }
    };

    public DialogWrapperCreator(Project project, JPanel mainPanel) {
        this.project = project;
        this.mainPanel = mainPanel;
    }

    public DialogWrapperCreator withProject(Project project) {
        this.project = project;
        return this;
    }

    public DialogWrapperCreator withMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
        return this;
    }

    public DialogWrapperCreator withDimensionKey(String dimensionKey) {
        this.dimensionKey = dimensionKey;
        return this;
    }

    public DialogWrapperCreator withModalityType(DialogWrapper.IdeModalityType modalityType) {
        this.modalityType = modalityType;
        return this;
    }

    public DialogWrapperCreator withCanBeParent(boolean canBeParent) {
        this.canBeParent = canBeParent;
        return this;
    }


    public DialogWrapperCreator withValidator(Supplier<List<ValidationInfo>>  validator) {
        this.validator = validator;
        return this;
    }


    public DialogWrapper create() {
        return wrap(project, mainPanel, dimensionKey, validator, modalityType, canBeParent);
    }

    static DialogWrapper wrap(Project project, JPanel mainPanel, String dimensionKey, Supplier<List<ValidationInfo>> validator, DialogWrapper.IdeModalityType modalityType, boolean canBeParent) {
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
            public void init() {
                super.init();
            }

            public void show() {

                this.init();
                this.setModal(true);
                this.pack();
                super.show();
            }
        };
    }
}
