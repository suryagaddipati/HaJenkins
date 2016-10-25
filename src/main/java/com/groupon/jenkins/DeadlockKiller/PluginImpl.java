package com.groupon.jenkins.DeadlockKiller;

import hudson.Plugin;
import jenkins.model.Jenkins;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class PluginImpl extends Plugin {
    public void start() throws Exception {
        final Jenkins jenkers = Jenkins.getInstance();
        final LockFreeQueue queue = new LockFreeQueue();
        final Field queueField = Jenkins.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        ReflectionUtils.setField(queueField, jenkers, queue);
    }
}
