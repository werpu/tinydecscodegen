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
