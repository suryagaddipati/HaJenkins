package com.groupon.jenkins.DeadlockKiller;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class HaJenkinsConfiguration extends GlobalConfiguration {
    private Boolean serveBuilds;

    public HaJenkinsConfiguration() {
        load();
    }

    public static HaJenkinsConfiguration get() {
        return GlobalConfiguration.all().get(HaJenkinsConfiguration.class);
    }

    public Boolean getServeBuilds() {
        return this.serveBuilds == null ? false : this.serveBuilds;
    }

    public void setServeBuilds(final Boolean serveBuilds) {
        this.serveBuilds = serveBuilds;
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }
}
