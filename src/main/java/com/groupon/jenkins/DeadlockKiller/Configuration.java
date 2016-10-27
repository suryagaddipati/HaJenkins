package com.groupon.jenkins.DeadlockKiller;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
public class Configuration extends GlobalConfiguration {
    private Boolean serveBuilds;

    public static Configuration get() {
        return GlobalConfiguration.all().get(Configuration.class);
    }

    public Boolean getServeBuilds() {
        return this.serveBuilds;
    }

    public void setServeBuilds(final Boolean serveBuilds) {
        this.serveBuilds = serveBuilds;
    }
}
