package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.mongo.MongoRepository;
import com.mongodb.DBCollection;
import hudson.model.Action;
import hudson.model.Queue;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;

public class QueueRepository extends MongoRepository {
    @Inject
    public QueueRepository(final Datastore datastore) {
        super(datastore);
    }

    public void save(final Queue.Task p, final int quitePeriod, final Action[] actions) {
        final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
        getDatastore().save(entry);

    }

    public QueueEntry getNext() {
        final Query<QueueEntry> query = getDatastore().createQuery(QueueEntry.class);
        return getDatastore().findAndDelete(query);
    }

    protected DBCollection getCollection() {
        return getDatastore().getDB().getCollection("queue");
    }
}
