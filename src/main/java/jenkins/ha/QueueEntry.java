package jenkins.ha;

import hudson.model.Action;
import org.bson.types.ObjectId;

public class QueueEntry {
    private final ObjectId projectId;
    private final int quitePeriod;
    private final Action[] actions;

    public QueueEntry(final ObjectId projectId, final int quitePeriod, final Action[] actions) {
        this.projectId = projectId;
        this.quitePeriod = quitePeriod;
        this.actions = actions;
    }

    public ObjectId getProjectId() {
        return this.projectId;
    }

    public int getQuitePeriod() {
        return this.quitePeriod;
    }

    public Action[] getActions() {
        return this.actions;
    }
}
