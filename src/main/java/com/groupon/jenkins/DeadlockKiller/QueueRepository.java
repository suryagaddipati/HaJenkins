package com.groupon.jenkins.DeadlockKiller;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.model.Action;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QueueRepository {

    public void save(final Queue.Task p, final int quitePeriod, final Action[] actions) {
        try (final Jedis jedis = getJedis()) {
            final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
            final String entryXml = Jenkins.XSTREAM2.toXML(entry);
            jedis.rpush("jenkins:queue", entryXml);
        }
    }


    public QueueEntry getNext() {
        try (final Jedis jedis = getJedis()) {
            final String entryXml = jedis.blpop(0, "jenkins:queue").get(1);
            return (QueueEntry) Jenkins.XSTREAM2.fromXML(entryXml);
        }
    }


    public void saveWatingItem(final Queue.WaitingItem wi) {
        try (final Jedis jedis = getJedis()) {
            final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(wi);
            final String remoteWaitingItemXml = Jenkins.XSTREAM2.toXML(remoteWatingItem);
            final String key = "jenkins:remote_wating_item:" + remoteWatingItem.getQueueId();
            jedis.set(key, remoteWaitingItemXml);
        }
    }

    public void removeLeftItem(final Queue.LeftItem li) {
        try (final Jedis jedis = getJedis()) {
            final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(li);
            final String key = "jenkins:remote_wating_item:" + remoteWatingItem.getQueueId();
            jedis.del(key);
        }
    }

    public List<Queue.Item> getRemoteWaitingItems() {
        try (final Jedis jedis = getJedis()) {
            if (jedis == null) return new ArrayList<>();
            final Set<String> remoteItemXmlKeys = jedis.keys("jenkins:remote_wating_item:*");
            final List<Queue.Item> remoteWatingItems = new ArrayList<>();
            for (final String remoteItemXmlKey : remoteItemXmlKeys) {
                final RemoteQueueWaitingItem remoteItem = (RemoteQueueWaitingItem) Jenkins.XSTREAM2.fromXML(jedis.get(remoteItemXmlKey));
                if (!remoteItem.getExecutingOnJenkinsUrl().contains(Jenkins.getInstance().getRootUrl())) { // there is already a queue item here
                    remoteWatingItems.add(RemoteQueueWaitingItem.getQueueItem(remoteItem));
                }

            }
            return remoteWatingItems;

        }
    }


    public void notifyCancellation(final long id) {
        try (final Jedis jedis = getJedis()) {
            jedis.publish("jenkins:queue_cancellation", id + "");
        }
    }

    public void subscribeToChannel(final String channelName, final JedisPubSub pubSub) {
        try (final Jedis jedis = getJedis()) {
            jedis.subscribe(pubSub, channelName);
        }
    }

    public void notifyBuildAbort(final DynamicBuild dynamicBuild) {
        try (final Jedis jedis = getJedis()) {
            jedis.publish("jenkins:build_cancellation", dynamicBuild.getProjectId().toString() + ":" + dynamicBuild.getNumber());
        }
    }

    private Jedis getJedis() {
        return PluginImpl.jedisPool == null ? null : PluginImpl.jedisPool.getResource();
    }


}
