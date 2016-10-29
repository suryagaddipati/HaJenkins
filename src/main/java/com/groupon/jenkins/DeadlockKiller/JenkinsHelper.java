package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import org.bson.types.ObjectId;

import java.util.Collection;

public class JenkinsHelper {
    public static Queue.Task findTask(final ObjectId projectId) {
        return getTask(projectId, Jenkins.getInstance().getItems());
    }

    private static Queue.Task getTask(final ObjectId projectId, final Collection<? extends Item> items) {
        for (final Item item : items) {
            if (item instanceof DbBackedProject && ((DbBackedProject) item).getId().equals(projectId)) {
                return (Queue.Task) item;
            }
            if (item instanceof ItemGroup) {
                return getTask(projectId, ((ItemGroup) item).getItems());
            }
        }
        return null;
    }
}
