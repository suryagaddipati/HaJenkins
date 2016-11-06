package jenkins.ha;

import hudson.Plugin;
import jenkins.ha.redis.QueueRepository;
import jenkins.ha.redis.listeners.DbQueueScheduler;
import jenkins.ha.redis.listeners.RemoteBuildStopListener;
import jenkins.model.Jenkins;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginImpl extends Plugin {
    public static JedisPool jedisPool;

    public void start() throws Exception {
        final Jenkins jenkers = Jenkins.getInstance();
        final RedisQueue queue = new RedisQueue();
        final Field queueField = Jenkins.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        ReflectionUtils.setField(queueField, jenkers, queue);
    }

    @Override
    public void postInitialize() throws Exception {
        final HaJenkinsConfiguration config = HaJenkinsConfiguration.get();
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        if (config.getRedisHost() != null) {
            PluginImpl.jedisPool = new JedisPool(poolConfig, config.getRedisHost());
        }

        if (config.getServeBuilds()) {
            final ExecutorService executor = Executors.newFixedThreadPool(3);
            executor.submit((Runnable) () -> {
                while (true) {
                    new DbQueueScheduler().doRun();
                }
            });
            executor.submit((Runnable) () -> {
                while (true) {
                    new RemoteBuildStopListener.RemoteQueueCancellationListener().doRun();
                    System.out.print("processed item");
                }
            });
            executor.submit((Runnable) () -> {
                while (true) {
                    new QueueRepository().subscribeToChannel("jenkins:build_cancellation", new RemoteBuildStopListener());
                }

            });
        }
    }

    @Override
    public void stop() throws Exception {
        if (jedisPool != null) jedisPool.destroy();
    }
}
