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
import java.util.logging.Logger;


public enum Queue {
    INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(Queue.class.getName());
    private ExecutorService executorService;
    private StatefulRedisConnection<String, String> redisQueueConnection;
    private boolean listenerStarted;

    public QueueEntry getNext(final StatefulRedisConnection<String, String> redisQueueConnection) {
        final KeyValue<String, String> queueEntry = redisQueueConnection.sync().brpop(0, "jenkins:queue");
        return (QueueEntry) Jenkins.XSTREAM2.fromXML(queueEntry.getValue());
    }

    public void save(final hudson.model.Queue.Task p, final int quitePeriod, final Action[] actions) {
        final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
        final String entryXml = Jenkins.XSTREAM2.toXML(entry);
        RedisConnections.INSTANCE.getRedisConnection().async().rpush("jenkins:queue", entryXml);
    }

    public void startListener() {
        if (!this.listenerStarted) {
            LOGGER.info("Starting Redis Queue Listener");
            this.redisQueueConnection = RedisConnections.INSTANCE.getRedisClient().connect();
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
            this.listenerStarted = true;
        }
    }

    public void stopListener() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
        if (this.redisQueueConnection != null) {
            this.redisQueueConnection.close();
        }
        this.listenerStarted = false;
    }


//
//    public void removeLeftItem(final Queue.LeftItem li) {
//        try (final Jedis jedis = getJedis()) {
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
