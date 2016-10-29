package com.groupon.jenkins.DeadlockKiller;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;

@Extension
public class QueueDbSyncher extends QueueListener {
    @Override
    public void onEnterWaiting(final Queue.WaitingItem wi) {
        if (wi.getAction(HaExecutionAction.class) != null) {
            new QueueRepository().saveWatingItem(wi);
        }
        super.onEnterWaiting(wi);
    }

    @Override
    public void onLeft(final Queue.LeftItem li) {
        if (li.getAction(HaExecutionAction.class) != null) {
            new QueueRepository().removeLeftItem(li);
        }
        super.onLeft(li);
    }
}
