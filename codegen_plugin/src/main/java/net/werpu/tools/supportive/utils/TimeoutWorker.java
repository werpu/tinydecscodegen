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

package net.werpu.tools.supportive.utils;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * a worker thread
 * which triggers after a while
 * nothing has happened
 */
@RequiredArgsConstructor
@CustomLog
public class TimeoutWorker {
    private static final long TIME_PERIOD = 10l * 1000l;
    final private Consumer<TimeoutWorker> runner;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    ScheduledFuture f = null;

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

}