package jenkins.ha;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.ha.redis.QueueRepository;
import jenkins.model.Jenkins;

@Extension
public class QueueDbSyncher extends QueueListener {
    @Override
    public void onEnterWaiting(final Queue.WaitingItem wi) {
        if (getHaExecutionAction(wi) != null) {
            new QueueRepository().saveWatingItem(wi);
        }
        super.onEnterWaiting(wi);
    }

    private HaExecutionAction getHaExecutionAction(final Queue.Item wi) {
        final HaExecutionAction haExecutionAction = wi.getAction(HaExecutionAction.class);
        if (haExecutionAction != null) {
            haExecutionAction.setExecutingOnJenkinsUrl(Jenkins.getInstance().getRootUrl());
        }
        return haExecutionAction;
    }

    @Override
    public void onLeft(final Queue.LeftItem li) {
        if (getHaExecutionAction(li) != null) {
            new QueueRepository().removeLeftItem(li);
        }
        super.onLeft(li);
    }
}
