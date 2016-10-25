package com.groupon.jenkins.DeadlockKiller;

import com.google.inject.Injector;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.PeriodicWork;
import hudson.model.Queue;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Extension
public class DbQueueScheduler extends PeriodicWork {

    public static final Logger LOGGER = Logger.getLogger(DbQueueScheduler.class.getName());

    public long getRecurrencePeriod() {
        return TimeUnit.SECONDS.toMillis(20);
    }

    @Override
    protected void doRun() throws Exception {

        final Injector injector = SetupConfig.get().getInjector();
        final QueueRepository queueRepository = injector.getInstance(QueueRepository.class);
        final QueueEntry queueEntry = queueRepository.getNext();
        if (queueEntry != null) {
            final Jenkins jenkins = Jenkins.getInstance();
            ((LockFreeQueue) jenkins.getQueue()).scheduleFromDb(getTask(jenkins, queueEntry), queueEntry.getQuitePeriod(), queueEntry.getActions());
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
