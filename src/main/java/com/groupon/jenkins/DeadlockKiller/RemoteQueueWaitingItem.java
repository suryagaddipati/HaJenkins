package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RemoteQueueWaitingItem {
    private final CauseOfBlockage causeOfBlockage;
    private final ObjectId projectId;
    private final String remoteId;

    public RemoteQueueWaitingItem(final Queue.WaitingItem waitingItem) {
        this.causeOfBlockage = waitingItem.getCauseOfBlockage();
        this.remoteId = getRemoteId(waitingItem);
        this.projectId = ((DynamicProject) waitingItem.task).getId();
    }

    public RemoteQueueWaitingItem(final Queue.LeftItem leftItem) {
        this.causeOfBlockage = leftItem.getCauseOfBlockage();
        this.remoteId = getRemoteId(leftItem);
        this.projectId = ((DynamicProject) leftItem.task).getId();
    }

    public static Queue.Item getQueueItem(final RemoteQueueWaitingItem remoteItem) {
        return new Queue.WaitingItem(Calendar.getInstance(), remoteItem.getTask(), remoteItem.getActions());

    }

    private String getRemoteId(final Queue.Item waitingItem) {
        return waitingItem.getId() + ":" + waitingItem.getAction(HaExecutionAction.class).getJenkinsInstanceId();
    }

    public String getRemoteId() {
        return this.remoteId;
    }


    private List<Action> getActions() {
        return new ArrayList<>();
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return this.causeOfBlockage;
    }

    public Queue.Task getTask() {
        return JenkinsHelper.findTask(this.projectId);
    }
}
