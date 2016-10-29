package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import hudson.model.TopLevelItem;
import hudson.model.queue.ScheduleResult;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


public class DbQueueScheduler {

    public static final Logger LOGGER = Logger.getLogger(DbQueueScheduler.class.getName());


    protected void doRun() {

        final QueueRepository queueRepository = new QueueRepository();
        final QueueEntry queueEntry = queueRepository.getNext();
        if (queueEntry != null) {
            final Jenkins jenkins = Jenkins.getInstance();
            final ScheduleResult result = ((LockFreeQueue) jenkins.getQueue()).scheduleFromDb(getTask(jenkins, queueEntry), queueEntry.getQuitePeriod(), queueEntry.getActions());
            if (result.equals(ScheduleResult.refused())) {
//                queueRepository.save(queueEntry);
            }
        }
    }

    private Queue.Task getTask(final Jenkins jenkins, final QueueEntry queueEntry) {
        final List<TopLevelItem> items = jenkins.getItems();
        return getTask(queueEntry, items);
    }

    private Queue.Task getTask(final QueueEntry queueEntry, final Collection<? extends Item> items) {
        for (final Item item : items) {
            if (item instanceof DbBackedProject && ((DbBackedProject) item).getId().equals(queueEntry.getProjectId())) {
                return (Queue.Task) item;
            }
            if (item instanceof ItemGroup) {
                return getTask(queueEntry, ((ItemGroup) item).getItems());
            }
        }
        return null;
    }
}
