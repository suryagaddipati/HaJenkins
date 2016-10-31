package jenkins.ha;

import hudson.model.Action;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.List;

public class HaExecutionAction implements Queue.QueueAction {
    private final long queueId;
    private String executingOnJenkinsUrl;

    public HaExecutionAction() {
        this.queueId = System.nanoTime();
    }

    public String getExecutingOnJenkinsUrl() {
        return this.executingOnJenkinsUrl;
    }

    public void setExecutingOnJenkinsUrl(final String executingOnJenkinsUrl) {
        this.executingOnJenkinsUrl = executingOnJenkinsUrl;
    }

    public long getQueueId() {
        return this.queueId;
    }

    @Override
    public boolean shouldSchedule(final List<Action> actions) {
        return true;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    public boolean isExectionOnThisJenkinsInstance() {
        return Jenkins.getInstance().getRootUrl().equals(this.executingOnJenkinsUrl);
    }
}
