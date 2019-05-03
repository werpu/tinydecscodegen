package net.werpu.tools.gui.support;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

abstract public class ValidatableDialogWrapper extends DialogWrapper {

    protected ValidatableDialogWrapper(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
    }

    protected ValidatableDialogWrapper(@Nullable Project project, boolean canBeParent, @NotNull IdeModalityType ideModalityType) {
        super(project, canBeParent, ideModalityType);
    }

    protected ValidatableDialogWrapper(@Nullable Project project, @Nullable Component parentComponent, boolean canBeParent, @NotNull IdeModalityType ideModalityType) {
        super(project, parentComponent, canBeParent, ideModalityType);
    }

    protected ValidatableDialogWrapper(@Nullable Project project, @Nullable Component parentComponent, boolean canBeParent, @NotNull IdeModalityType ideModalityType, boolean createSouth) {
        super(project, parentComponent, canBeParent, ideModalityType, createSouth);
    }

    protected ValidatableDialogWrapper(@Nullable Project project) {
        super(project);
    }

    protected ValidatableDialogWrapper(boolean canBeParent) {
        super(canBeParent);
    }

    protected ValidatableDialogWrapper(boolean canBeParent, boolean applicationModalIfPossible) {
        super(canBeParent, applicationModalIfPossible);
    }

    protected ValidatableDialogWrapper(Project project, boolean canBeParent, boolean applicationModalIfPossible) {
        super(project, canBeParent, applicationModalIfPossible);
    }

    protected ValidatableDialogWrapper(@NotNull Component parent, boolean canBeParent) {
        super(parent, canBeParent);
    }


    @Nullable
    @NotNull
    public List<ValidationInfo> doValidateAll() {
        return super.doValidateAll();
    }


    @Nullable
    @Override
    public ValidationInfo doValidate() {
        return super.doValidate();
    }

}
