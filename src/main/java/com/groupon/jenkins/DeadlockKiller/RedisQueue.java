package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import hudson.model.Action;
import hudson.model.LoadBalancer;
import hudson.model.Queue;
import hudson.model.queue.ScheduleResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisQueue extends Queue {


    public RedisQueue() {
        super(LoadBalancer.CONSISTENT_HASH);
    }

    @Nonnull
    @Override
    public ScheduleResult schedule2(final Task p, final int quietPeriod, final Action... actions) {
        return scheduleBuild(p, quietPeriod, actions);
    }

    private ScheduleResult scheduleBuild(final Task p, final int quietPeriod, final Action[] actions) {
        if (p instanceof DynamicProject) {
            saveToDb(p, quietPeriod, actions);
            return ScheduleResult.refused();
        }
        return super.schedule2(p, quietPeriod, Arrays.asList(actions));
    }

    private void saveToDb(final Task p, final int quietPeriod, final Action[] actions) {
        final QueueRepository queueRepository = new QueueRepository();
        final List<Action> actionList = new ArrayList<>(Arrays.asList(actions));
        actionList.add(new HaExecutionAction());
        queueRepository.save(p, quietPeriod, actionList.toArray(new Action[]{}));
    }

    @Nonnull
    @Override
    public ScheduleResult schedule2(final Task p, final int quietPeriod, final List<Action> actions) {
        return scheduleBuild(p, quietPeriod, actions.toArray(new Action[]{}));
    }

    public ScheduleResult scheduleFromDb(final Task p, final int quietPeriod, final Action... actions) {
        return super.schedule2(p, quietPeriod, Arrays.asList(actions));
    }


}
