package com.groupon.jenkins.DeadlockKiller;

import hudson.Extension;
import hudson.model.PeriodicWork;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Extension
public class DeadlockKillerTask extends PeriodicWork {

    public static final Logger LOGGER = Logger.getLogger(DeadlockKillerTask.class.getName());

    public long getRecurrencePeriod() {
        return TimeUnit.SECONDS.toMillis(10);
    }

    @Override
    protected void doRun() throws Exception {
        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.

        if (threadIds != null) {
            final ThreadInfo[] infos = bean.getThreadInfo(threadIds);
            final Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
            final List<Thread> threads = new ArrayList<>(stackTraces.keySet());

            for (final ThreadInfo info : infos) {
                final StackTraceElement[] stack = info.getStackTrace();
                for (final Thread thread : threads) {
                    if (thread.getId() == info.getThreadId()) {
                        DeadlockKillerTask.LOGGER.info("Killing Thread :" + thread.getId());
                        thread.stop();
                    }
                }
            }
        }
    }
}
