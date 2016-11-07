package jenkins.ha.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import jenkins.ha.HaJenkinsConfiguration;
import jenkins.ha.redis.pubsub.Queue;

public class RedisConnections {
    // Redis connections are designed to be long-lived and thread-safe, don't block on this connection
    public static StatefulRedisConnection<String, String> redisConnection;
    public static RedisClient redisClient;

    public static void startQueueListener() {
        if (HaJenkinsConfiguration.get().getServeBuilds()) {
            Queue.INSTANCE.startListener();
        }
    }

    public static void init() {
        final HaJenkinsConfiguration config = HaJenkinsConfiguration.get();
        final RedisURI redisUri = RedisURI.Builder.redis(config.getRedisHost()).build();
        RedisConnections.redisClient = RedisClient.create(redisUri);
        RedisConnections.redisConnection = redisClient.connect();
        startQueueListener();
    }

    public static void shutDown() {
        if (redisConnection != null) redisConnection.close();
        if (redisClient != null) redisClient.shutdown();
        Queue.INSTANCE.stopListener();
    }
}
