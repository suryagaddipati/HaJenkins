package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import hudson.model.Action;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueueRepository {

    public void save(final Queue.Task p, final int quitePeriod, final Action[] actions) {
        final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
        final String entryXml = Jenkins.XSTREAM2.toXML(entry);
        final Jedis jedis = getJedis();
        jedis.rpush("queue", entryXml);

    }


    public QueueEntry getNext() {
        final Jedis jedis = getJedis();
        final String entryXml = jedis.blpop(0, "queue").get(1);
        return (QueueEntry) Jenkins.XSTREAM2.fromXML(entryXml);
    }


    public void saveWatingItem(final Queue.WaitingItem wi) {
        final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(wi);
        final String remoteWaitingItemXml = Jenkins.XSTREAM2.toXML(remoteWatingItem);
        final String key = "remote_wating_item:" + remoteWatingItem.getRemoteId();
        getJedis().hmget(key, remoteWaitingItemXml);
    }

    public void removeLeftItem(final Queue.LeftItem li) {
        final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(li);
        final String key = "remote_wating_item:" + remoteWatingItem.getRemoteId();
        getJedis().del(key);
    }

    public List<Queue.Item> getRemoteWaitingItems() {
        final Map<String, String> remoteItemXmls = getJedis().hgetAll("remote_wating_item:*");
        final List<Queue.Item> remoteWatingItems = new ArrayList<>();
        for (final String remoteItemXml : remoteItemXmls.values()) {
            final RemoteQueueWaitingItem remoteItem = (RemoteQueueWaitingItem) Jenkins.XSTREAM2.fromXML(remoteItemXml);
            remoteWatingItems.add(RemoteQueueWaitingItem.getQueueItem(remoteItem));
        }
        return remoteWatingItems;
    }


    private Jedis getJedis() {
        return new Jedis("localhost");
    }
}
