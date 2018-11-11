package net.werpu.tools.supportive.utils;

import com.intellij.openapi.diagnostic.Logger;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * a worker thread
 * which triggers after a while
 * nothing has happened
 */
@RequiredArgsConstructor
public class TimeoutWorker {

    private static final Logger log = Logger.getInstance(TimeoutWorker.class);

    private static final long TIME_PERIOD = 10l * 1000l;
    final private Consumer<TimeoutWorker> runner;


    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    ScheduledFuture f = null;

    /**
     * start, in case of a paused thread
     * it is started again
     * in case of a stopped thread it is reinitalized again
     */
    public void start() {
        scheduleThread();
    }

    private synchronized void scheduleThread() {
        if (f != null) {
            f.cancel(false);
        }

        f = executor.schedule(() -> {
            try {
                runner.accept(this);
            } catch (Throwable ex) {
                log.error(ex);
            } finally {
                f = null;
            }
            return null;
        }, TIME_PERIOD, TimeUnit.MILLISECONDS);
    }

    private void waitUntilDone() {
        try {
            f.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * by triggering this methid we
     * run another timeout cycle which in the end then results
     * in the sync
     */
    public void notifyOfChange() {
        this.scheduleThread();
    }


    /**
     * ends it once and forall
     */
    public void stop() {
        if (f != null) {
            f.cancel(false);
        }

    }

    /**
     * a defer routine similar to the javascript
     * setTimeout
     *
     * @param runner
     * @param timeoutInMs
     * @return
     */
    public static ScheduledFuture<?> setTimeout(Runnable runner, long timeoutInMs) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.schedule(() -> {
            runner.run();
            return null;
        }, timeoutInMs, TimeUnit.MILLISECONDS);
    }

}