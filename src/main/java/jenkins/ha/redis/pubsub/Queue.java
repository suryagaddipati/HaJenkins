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
import java.util.logging.Level;
import java.util.logging.Logger;


public enum Queue {
    INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(Queue.class.getName());
    private ExecutorService executorService;
    private StatefulRedisConnection<String, String> redisQueueConnection;
    private boolean listenerStarted;

    public void save(final hudson.model.Queue.Task p, final int quitePeriod, final Action[] actions) {
        final QueueEntry entry = new QueueEntry(((DbBackedProject) p).getId(), quitePeriod, actions);
        final String entryXml = Jenkins.XSTREAM2.toXML(entry);
        RedisConnections.INSTANCE.getRedisConnection().sync().rpush("jenkins:queue", entryXml);
    }

    public void startListener() {
        if (!this.listenerStarted) {
            LOGGER.info("Starting Redis Queue Listener");
            this.redisQueueConnection = RedisConnections.INSTANCE.getRedisClient().connect();
            final HaJenkinsQueue jenkinsQueue = (HaJenkinsQueue) Jenkins.getInstance().getQueue();
            this.executorService = Executors.newSingleThreadExecutor();
            this.executorService.submit(() -> {
                while (true) {
                    try {
                        LOGGER.info("Wating for Next Queue item ..");
                        //A timeout of zero can be used to block indefinitely.
                        final KeyValue<String, String> queueEntryXml = this.redisQueueConnection.async().brpop(0, "jenkins:queue").get();
                        final QueueEntry queueEntry = (QueueEntry) Jenkins.XSTREAM2.fromXML(queueEntryXml.getValue());
                        LOGGER.info("Processing item from queue: " + queueEntry.getProjectId());
                        jenkinsQueue.schedule(queueEntry);
                    } catch (final InterruptedException ie) {
                        return; //uncheck watch checkbox in configuration
                    } catch (final Exception e) {
                        //Don't kill the loop
                        LOGGER.log(Level.SEVERE, "Failed to scheduled from queue", e);
                    }
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

}
