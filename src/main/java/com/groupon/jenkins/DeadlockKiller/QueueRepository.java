package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import hudson.model.Action;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import redis.clients.jedis.Jedis;

public class QueueRepository {

    public void save(final Queue.Task p, final int quitePeriod, final Action[] actions) {
        final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
        final String entryXml = Jenkins.XSTREAM2.toXML(entry);
        final Jedis jedis = new Jedis("localhost");
        jedis.rpush("queue", entryXml);

    }

    public QueueEntry getNext() {
        final Jedis jedis = new Jedis("localhost");
        final String entryXml = jedis.blpop(0, "queue").get(1);
        return (QueueEntry) Jenkins.XSTREAM2.fromXML(entryXml);
    }


}
