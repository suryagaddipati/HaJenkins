package jenkins.ha.redis.pubsub;

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.lambdaworks.redis.KeyValue;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import hudson.model.Action;
import jenkins.ha.HaJenkinsQueue;
import jenkins.ha.redis.RedisConnections;
import jenkins.ha.redis.models.QueueEntry;
import jenkins.model.Jenkins;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public enum Queue {
    INSTANCE;

    private ExecutorService executorService;
    private StatefulRedisConnection<String, String> redisQueueConnection;

    public QueueEntry getNext(final StatefulRedisConnection<String, String> redisQueueConnection) {
        final KeyValue<String, String> queueEntry = redisQueueConnection.sync().brpop(0, "jenkins:queue");
        return (QueueEntry) Jenkins.XSTREAM2.fromXML(queueEntry.getValue());
    }

    public void save(final hudson.model.Queue.Task p, final int quitePeriod, final Action[] actions) {
        final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
        final String entryXml = Jenkins.XSTREAM2.toXML(entry);
        RedisConnections.redisConnection.async().rpush("jenkins:queue", entryXml);
    }

    public void startListener() {
        this.redisQueueConnection = RedisConnections.redisClient.connect();
        final HaJenkinsQueue jenkinsQueue = (HaJenkinsQueue) Jenkins.getInstance().getQueue();
        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(() -> {
            while (true) {
                LOGGER.info("Wating for Next Queue item ..");
                final QueueEntry queueEntry = Queue.INSTANCE.getNext(this.redisQueueConnection);
                LOGGER.info("Processing item from queue: " + queueEntry.getProjectId());
                jenkinsQueue.schedule(queueEntry);
            }
        });
    }

    public void stopListener() {
        if (this.executorService != null) this.executorService.shutdownNow();
        if (this.redisQueueConnection != null)
            this.redisQueueConnection.close();
    }


//    public void saveWatingItem(final Queue.WaitingItem wi) {
//        try (final Jedis jedis = getJedis()) {
//            final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(wi);
//            final String remoteWaitingItemXml = Jenkins.XSTREAM2.toXML(remoteWatingItem);
//            final HashMap<String, String> map = new HashMap<>();
//            map.put(remoteWatingItem.getQueueId() + "", remoteWaitingItemXml);
//            jedis.hmset("jenkins:remote_wating_item", map);
//        }
//    }
//
//    public void removeLeftItem(final Queue.LeftItem li) {
//        try (final Jedis jedis = getJedis()) {
//            final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(li);
//            jedis.hdel("jenkins:remote_wating_item", remoteWatingItem.getQueueId() + "");
//        }
//    }
//
//    public List<Queue.Item> getRemoteWaitingItems() {
//        try (final Jedis jedis = getJedis()) {
//            if (jedis == null) return new ArrayList<>();
//            final List<String> remoteItemXmls = jedis.hvals("jenkins:remote_wating_item");
//            final List<Queue.Item> remoteWatingItems = new ArrayList<>();
//            for (final String remoteItemXml : remoteItemXmls) {
//                final RemoteQueueWaitingItem remoteItem = (RemoteQueueWaitingItem) Jenkins.XSTREAM2.fromXML(remoteItemXml);
//                if (!remoteItem.getExecutingOnJenkinsUrl().contains(Jenkins.getInstance().getRootUrl())) { // there is already a queue item here
//                    remoteWatingItems.add(RemoteQueueWaitingItem.getQueueItem(remoteItem));
//                }
//            }
//            return remoteWatingItems;
//
//        }
//    }
//
//
//    public void notifyCancellation(final long id) {
//        try (final Jedis jedis = getJedis()) {
//            jedis.publish("jenkins:queue_cancellation", id + "");
//        }
//    }
//
//    public void subscribeToChannel(final String channelName, final JedisPubSub pubSub) {
//        try (final Jedis jedis = getJedis()) {
//            jedis.subscribe(pubSub, channelName);
//        }
//    }
//
//    public void notifyBuildAbort(final DynamicBuild dynamicBuild) {
//        try (final Jedis jedis = getJedis()) {
//            jedis.publish("jenkins:build_cancellation", dynamicBuild.getProjectId().toString() + ":" + dynamicBuild.getNumber());
//        }
//    }
//
//    private Jedis getJedis() {
//        return PluginImpl.jedisPool == null ? null : PluginImpl.jedisPool.getResource();
//    }


}
