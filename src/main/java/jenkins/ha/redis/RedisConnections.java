package jenkins.ha.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import jenkins.ha.HaJenkinsConfiguration;
import jenkins.ha.redis.pubsub.Queue;
import org.apache.commons.lang.StringUtils;

public enum RedisConnections {
    INSTANCE;

    // Redis connections are designed to be long-lived and thread-safe, don't block on this connection
    private StatefulRedisConnection<String, String> redisConnection;
    private RedisClient redisClient;

    public StatefulRedisConnection<String, String> getRedisConnection() {
        return this.redisConnection;
    }

    public RedisClient getRedisClient() {
        return this.redisClient;
    }

    public void shutDown() {
        if (this.redisConnection != null) this.redisConnection.close();
        if (this.redisClient != null) this.redisClient.shutdown();
        Queue.INSTANCE.stopListener();
    }

    private void startQueueListener(final HaJenkinsConfiguration config) {
        if (config.getServeBuilds()) {
            Queue.INSTANCE.startListener();
        } else {
            Queue.INSTANCE.startListener();
        }
    }

    public void init() {
        final HaJenkinsConfiguration config = HaJenkinsConfiguration.get();
        final RedisURI redisUri = RedisURI.Builder.redis(config.getRedisHost()).build();
        if (StringUtils.isNotEmpty(config.getRedisHost())) {
            initRedisClient(redisUri);
            startQueueListener(config);
        }
    }

    private void initRedisClient(final RedisURI redisUri) {
        if (this.redisClient == null) {
            this.redisClient = RedisClient.create(redisUri);
            this.redisConnection = this.redisClient.connect();
        }
    }
}
