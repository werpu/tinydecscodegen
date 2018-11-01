package supportive.utils;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supportive.fs.common.IAngularFileContext;

import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class IntellijRunUtils {


    public static Consumer NOOP_CONSUMER = (Object fileSelected) -> {

    };

    public static Runnable NOOP_RUNNABLE = () -> {

    };

    public static void invokeLater(Runnable run) {
        ApplicationManager.getApplication().invokeLater(run);
    }

    public static void invokeLater(Runnable run, ModalityState modalityState) {
        ApplicationManager.getApplication().invokeLater(run, modalityState);
    }

    public static void smartInvokeLater(Project project, Runnable run) {
        DumbService.getInstance(project).smartInvokeLater(run);
    }

    public static void runReadSmart(Project project, Runnable run) {
        DumbService.getInstance(project).runReadActionInSmartMode(run);
    }

    public static void writeTransaction(Project project, Runnable runnable) {
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    public static void readAction(Runnable runnable) {
        ApplicationManager.getApplication().runReadAction(runnable);
    }

    public static <T> T readAction(Computable<T> runnable) {
        return ApplicationManager.getApplication().runReadAction(runnable);
    }

    public static Task.Backgroundable backgroundTask(Project project, String title, Consumer<ProgressIndicator> runner) {
        return new Task.Backgroundable(project, title) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                runner.accept(progressIndicator);
            }
        };
    }

    public static Task.Modal modalTask(Project project, String title, boolean canBeCancelled, Consumer<ProgressIndicator> runner) {
        return new Task.Modal(project, title, canBeCancelled) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                runner.accept(progressIndicator);
            }
        };
    }

    public static void runWithProcessAsnyc(Task.Backgroundable myTask, String title) {
        ProgressIndicator myProcessIndicator = new BackgroundableProcessIndicator(myTask);
        myProcessIndicator.setText(title);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(myTask, myProcessIndicator);
    }

    public static void runAsync(Task.Backgroundable myTask) {
        ProgressManager.getInstance().run(myTask);
    }

    public static void runAsync(Project project, String title, Consumer<ProgressIndicator> runner) {
        runAsync(backgroundTask(project, title,  runner));
    }

    public static void runSync(Task.Modal myTask) {
        ProgressManager.getInstance().run(myTask);
    }

    public static ActionGroup actionGroup(AnAction... actions) {
        return new ActionGroup() {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent e) {
                return actions;
            }
        };
    }

    public static AnAction[] actions(AnAction... actions) {
        return actions;
    }

    /**
     * a simplified editor change event callback, which should
     * improve readability
     *
     * @param project  the current project
     * @param consumer a consumer function to adhere to java 8+ functional conventions
     *                 this function provides the event callback
     * @return the listener object for further processing
     */
    public static FileEditorManagerListener onEditorChange(Project project, Consumer<FileEditorManagerEvent> consumer) {
        final FileEditorManagerListener editorManagerListener = new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                consumer.accept(event);
            }
        };
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, editorManagerListener);
        return editorManagerListener;
    }

    /**
     * callback registration for the onFileChange event or message
     * note, will be triggered only on ts files for the time being
     *
     * @param project  the project to watch
     * @param runnable a runnable which triggers something on a file change
     */
    public static void onFileChange(Project project, Runnable runnable) {
        // MessageBusFactory.newMessageBus(project).
        //TODO move this over to the message bus system
        //the listener system becomes deprecated soon
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                //TODO angular version dynamic depending on the project type
                if (!file.getName().endsWith(IntellijUtils.getTsExtension())) {
                    return;
                }

                //document listener which refreshes every time a route file changes
                getChangeListener().smartInvokeLater(() -> runnable.run());
            }

            private DumbService getChangeListener() {
                return DumbService.getInstance(project);
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        });
    }

    /**
     * same as before but for a consumer who gets the changed file passed in
     */
    public static void onFileChange(Project project, Consumer<VirtualFile> runnable) {
        //TODO move this over to the message bus system
        //the listener system becomes deprecated soon
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                //TODO angular version dynamic depending on the project type
                if (!file.getName().endsWith(IntellijUtils.getTsExtension())) {
                    return;
                }

                //document listener which refreshes every time a route file changes
                getChangeListener().smartInvokeLater(() -> runnable.accept(file));
            }

            private DumbService getChangeListener() {
                return DumbService.getInstance(project);
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        });
    }

    public static <T> FutureTask<T> newFutureTask(Computable<T> runnable) {
        return new FutureTask<>(() -> {
            return runnable.compute();
        });
    }

    public static <T> FutureTask<T> newFutureRoTask(Computable<T> runnable) {
        return new FutureTask<>(() -> readAction(runnable));
    }
}
