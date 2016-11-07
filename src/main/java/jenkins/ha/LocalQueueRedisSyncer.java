package jenkins.ha;

import hudson.Extension;
import hudson.model.queue.QueueListener;
import jenkins.ha.redis.pubsub.RemoteLocalQueue;
import jenkins.model.Jenkins;

@Extension
public class LocalQueueRedisSyncer extends QueueListener {
    @Override
    public void onEnterWaiting(final hudson.model.Queue.WaitingItem wi) {
        final HaExecutionAction haExecutionAction = getHaExecutionAction(wi);
        if (haExecutionAction != null) {
            haExecutionAction.setExecutingOnJenkinsUrl(Jenkins.getInstance().getRootUrl());
            RemoteLocalQueue.INSTANCE.save(wi);
        }
    }

    private HaExecutionAction getHaExecutionAction(final hudson.model.Queue.Item wi) {
        return wi.getAction(HaExecutionAction.class);
    }

    @Override
    public void onLeft(final hudson.model.Queue.LeftItem li) {
        if (getHaExecutionAction(li) != null) {
            RemoteLocalQueue.INSTANCE.remove(li);
        }
    }
}
