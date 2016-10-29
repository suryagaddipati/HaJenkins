package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.FutureImpl;
import javassist.Modifier;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemoteQueueWaitingItem {
    private final CauseOfBlockage causeOfBlockage;
    private final long id;
    private final ObjectId projectId;
    private final String remoteId;

    public RemoteQueueWaitingItem(final Queue.WaitingItem waitingItem) {
        this.causeOfBlockage = waitingItem.getCauseOfBlockage();
        this.id = new Date().getTime();
        this.remoteId = waitingItem.getId() + waitingItem.getAction(HaExecutionAction.class).getServerName();
        this.projectId = ((DynamicProject) waitingItem.task).getId();
    }

    public RemoteQueueWaitingItem(final Queue.LeftItem leftItem) {
        this.causeOfBlockage = leftItem.getCauseOfBlockage();
        this.id = new Date().getTime();
        this.remoteId = leftItem.getId() + leftItem.getAction(HaExecutionAction.class).getServerName();
        this.projectId = ((DynamicProject) leftItem.task).getId();
    }

    public static Queue.Item getQueueItem(final RemoteQueueWaitingItem remoteItem) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(Queue.Item.class);
        factory.setFilter(
                method -> Modifier.isAbstract(method.getModifiers())
        );
        final MethodHandler handler = (self, thisMethod, proceed, args) -> {
            System.out.println("Handling " + thisMethod + " via the method handler");
            return null;
        };
//        protected Item(Task task, List<Action> actions, long id, FutureImpl future) {
        try {
            return (Queue.Item) factory.create(new Class<?>[]{Queue.Task.class, List.class, long.class, FutureImpl.class}, new Object[]{
                    remoteItem.getTask(), remoteItem.getActions(), remoteItem.getId(), new FutureImpl(remoteItem.getTask())
            }, handler);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;


    }

    public String getRemoteId() {
        return this.remoteId;
    }

    public long getId() {
        return this.id;
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
