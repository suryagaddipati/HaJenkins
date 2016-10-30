package com.groupon.jenkins.DeadlockKiller;

import com.google.common.hash.Hashing;
import hudson.Plugin;
import jenkins.model.Jenkins;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginImpl extends Plugin {
    public static String jenkinsInstanceId;

    public void start() throws Exception {
        final Jenkins jenkers = Jenkins.getInstance();
        final RedisQueue queue = new RedisQueue();
        final Field queueField = Jenkins.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        ReflectionUtils.setField(queueField, jenkers, queue);
    }

    @Override
    public void postInitialize() throws Exception {
        PluginImpl.jenkinsInstanceId = Hashing.sha256()
                .hashString(Jenkins.getInstance().getRootUrl(), StandardCharsets.UTF_8)
                .toString();
        final HaJenkinsConfiguration config = HaJenkinsConfiguration.get();
        if (config.getServeBuilds()) {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit((Runnable) () -> {
                while (true) {
                    new DbQueueScheduler().doRun();
                }
            });
        }
    }
}
