package com.groupon.jenkins.DeadlockKiller;

import hudson.model.InvisibleAction;

public class HaExecutionAction extends InvisibleAction {

    private final String jenkinsInstanceId;
    private final String globalQueueId;

    public HaExecutionAction() {
        this.jenkinsInstanceId = PluginImpl.jenkinsInstanceId;
        this.globalQueueId = PluginImpl.jenkinsInstanceId + System.nanoTime();
    }

    public String getGlobalQueueId() {
        return this.globalQueueId;
    }

    public String getJenkinsInstanceId() {
        return this.jenkinsInstanceId;
    }

}
