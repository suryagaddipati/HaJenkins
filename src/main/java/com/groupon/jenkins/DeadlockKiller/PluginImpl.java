package com.groupon.jenkins.DeadlockKiller;

import hudson.Plugin;
import jenkins.model.Jenkins;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginImpl extends Plugin {

    public void start() throws Exception {
        final Jenkins jenkers = Jenkins.getInstance();
        final RedisQueue queue = new RedisQueue();
        final Field queueField = Jenkins.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        ReflectionUtils.setField(queueField, jenkers, queue);
    }

    @Override
    public void postInitialize() throws Exception {
        final HaJenkinsConfiguration config = HaJenkinsConfiguration.get();
        if (config.getServeBuilds()) {
            final ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit((Runnable) () -> {
                while (true) {
                    new DbQueueScheduler().doRun();
                }
            });
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        new RemoteQueueCancellationListener().doRun();
                    }
                }
            });
        }
    }
}
