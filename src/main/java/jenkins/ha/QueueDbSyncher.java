package jenkins.ha;

import hudson.Extension;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

@Extension
public class QueueDbSyncher extends QueueListener {
    @Override
    public void onEnterWaiting(final hudson.model.Queue.WaitingItem wi) {
        if (getHaExecutionAction(wi) != null) {
//            new Queue().saveWatingItem(wi);
        }
        super.onEnterWaiting(wi);
    }

    private HaExecutionAction getHaExecutionAction(final hudson.model.Queue.Item wi) {
        final HaExecutionAction haExecutionAction = wi.getAction(HaExecutionAction.class);
        if (haExecutionAction != null) {
            haExecutionAction.setExecutingOnJenkinsUrl(Jenkins.getInstance().getRootUrl());
        }
        return haExecutionAction;
    }

    @Override
    public void onLeft(final hudson.model.Queue.LeftItem li) {
        if (getHaExecutionAction(li) != null) {
//            new Queue().removeLeftItem(li);
        }
        super.onLeft(li);
    }
}
