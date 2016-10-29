package com.groupon.jenkins.DeadlockKiller;

import hudson.model.InvisibleAction;
import jenkins.model.Jenkins;

public class HaExecutionAction extends InvisibleAction {

    private final String serverName;

    public HaExecutionAction() {
        this.serverName = Jenkins.getInstance().getDisplayName();
    }

    public String getServerName() {
        return this.serverName;
    }
}
