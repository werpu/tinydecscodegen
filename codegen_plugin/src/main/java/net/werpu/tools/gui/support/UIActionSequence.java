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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * a builder to create a set of action sequences
 * this shows a progress indicator and executes a series of actions
 */
@RequiredArgsConstructor
public class UIActionSequence {
    @NonNull
    private final Project project;
    @NonNull
    private final String indicatorLabel;
    List<Consumer<ProgressIndicator>> sequences = new ArrayList<>();
    Consumer<ProgressIndicator> onFinishedOk = (indicator) -> {
    };

    public UIActionSequence withSequence(Consumer<ProgressIndicator> sequence) {
        sequences.add(sequence);
        return this;
    }

    public UIActionSequence and(Consumer<ProgressIndicator> sequence) {
        sequences.add(sequence);
        return this;
    }

    public UIActionSequence withFinishedOk(Consumer<ProgressIndicator> sequence) {
        this.onFinishedOk = sequence;
        return this;
    }

    public void run() {
        final Task.Backgroundable myTask = new Task.Backgroundable(project, "calling npm install") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {

                progressIndicator.setIndeterminate(true);

                sequences.stream().forEach((seq) -> {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        seq.accept(progressIndicator);
                    });
                });

                ApplicationManager.getApplication().invokeLater(() -> {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        onFinishedOk.accept(progressIndicator);
                    });
                });

            }
        };
        BackgroundableProcessIndicator myProcessIndicator = new BackgroundableProcessIndicator(myTask);
        myProcessIndicator.setText(indicatorLabel);

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(myTask, myProcessIndicator);
    }

}
