package jenkins.ha;

import hudson.Extension;
import jenkins.ha.redis.pubsub.Queue;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class HaJenkinsConfiguration extends GlobalConfiguration {
    private Boolean serveBuilds;
    private String redisHost;

    public HaJenkinsConfiguration() {
        load();
    }

    public static HaJenkinsConfiguration get() {
        return GlobalConfiguration.all().get(HaJenkinsConfiguration.class);
    }

    public String getRedisHost() {
        return this.redisHost;
    }

    public void setRedisHost(final String redisHost) {
        this.redisHost = redisHost;
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
        resetQueueListeners();
        return true;
    }

    private void resetQueueListeners() {
        if (this.serveBuilds) Queue.INSTANCE.startListener();
        else Queue.INSTANCE.stopListener();
    }

}
