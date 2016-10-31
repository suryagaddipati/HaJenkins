package jenkins.ha;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import hudson.model.Action;
import hudson.model.Queue;
import org.bson.types.ObjectId;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

//Represents queue Item running on some other Jenkins Instance
public class RemoteQueueWaitingItem {
    private final ObjectId projectId;
    private final long queueId;
    private final List<? extends Action> actions;
    private final String executingOnJenkinsUrl;

    public RemoteQueueWaitingItem(final Queue.WaitingItem waitingItem) {
        this.projectId = ((DynamicProject) waitingItem.task).getId();
        this.queueId = waitingItem.getAction(HaExecutionAction.class).getQueueId();
        this.executingOnJenkinsUrl = waitingItem.getAction(HaExecutionAction.class).getExecutingOnJenkinsUrl();
        this.actions = waitingItem.getAllActions();
    }

    public RemoteQueueWaitingItem(final Queue.LeftItem leftItem) {
        this.projectId = ((DynamicProject) leftItem.task).getId();
        this.queueId = leftItem.getAction(HaExecutionAction.class).getQueueId();
        this.executingOnJenkinsUrl = leftItem.getAction(HaExecutionAction.class).getExecutingOnJenkinsUrl();
        this.actions = leftItem.getAllActions();
    }

    public static Queue.Item getQueueItem(final RemoteQueueWaitingItem remoteItem) {
        final Queue.WaitingItem waitingItem = new Queue.WaitingItem(Calendar.getInstance(), remoteItem.getTask(), (List<Action>) remoteItem.actions);
        try {
            final Field idField = Queue.Item.class.getDeclaredField("id");
            idField.setAccessible(true);
            ReflectionUtils.setField(idField, waitingItem, remoteItem.queueId);
        } catch (final NoSuchFieldException e) {
            //
        }
        return waitingItem;

    }

    public String getExecutingOnJenkinsUrl() {
        return this.executingOnJenkinsUrl;
    }


    public long getQueueId() {
        return this.queueId;
    }

    public Queue.Task getTask() {
        return JenkinsHelper.findTask(this.projectId);
    }
}
