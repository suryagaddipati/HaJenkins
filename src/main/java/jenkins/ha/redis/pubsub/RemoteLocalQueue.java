package jenkins.ha.redis.pubsub;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.pubsub.RedisPubSubAdapter;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;
import hudson.model.Queue;
import jenkins.ha.JenkinsHelper;
import jenkins.ha.redis.RedisConnections;
import jenkins.ha.redis.models.RemoteQueueWaitingItem;
import jenkins.model.Jenkins;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public enum RemoteLocalQueue {
    INSTANCE;


    public static final String CHANNEL = "jenkins:queue_cancellation";
    private StatefulRedisPubSubConnection<String, String> connection;

    public void save(final Queue.WaitingItem wi) {
        if (RedisConnections.INSTANCE.hasRedis()) {
            final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(wi);
            final String remoteWaitingItemXml = Jenkins.XSTREAM2.toXML(remoteWatingItem);
            final HashMap<String, String> map = new HashMap<>();
            map.put(remoteWatingItem.getQueueId() + "", remoteWaitingItemXml);
            redis().hmset("jenkins:remote_wating_item", map);
        }
    }

    private RedisAsyncCommands<String, String> redis() {
        return RedisConnections.INSTANCE.getRedisConnection().async();
    }

    public void remove(final Queue.LeftItem li) {
        if (RedisConnections.INSTANCE.hasRedis()) {
            final RemoteQueueWaitingItem remoteWatingItem = new RemoteQueueWaitingItem(li);
            redis().hdel("jenkins:remote_wating_item", remoteWatingItem.getQueueId() + "");
        }
    }

    public Collection<? extends Queue.Item> getAll() {
        if (RedisConnections.INSTANCE.hasRedis()) {
            final RedisCommands<String, String> redis = RedisConnections.INSTANCE.getRedisConnection().sync();
            final List<String> remoteItemXmls = redis.hvals("jenkins:remote_wating_item");
            final List<Queue.Item> remoteWatingItems = new ArrayList<>();
            for (final String remoteItemXml : remoteItemXmls) {
                final RemoteQueueWaitingItem remoteItem = (RemoteQueueWaitingItem) Jenkins.XSTREAM2.fromXML(remoteItemXml);
                if (!remoteItem.getExecutingOnJenkinsUrl().contains(Jenkins.getInstance().getRootUrl())) { // there is already a queue item here
                    remoteWatingItems.add(RemoteQueueWaitingItem.getQueueItem(remoteItem));
                }
            }
            return remoteWatingItems;
        }
        return new ArrayList<>();
    }


    public void stopCancellationListener() {
        if (this.connection != null) this.connection.close();
    }

    public void startCancellationListener() {
        if (RedisConnections.INSTANCE.hasRedis()) {
            this.connection = RedisConnections.INSTANCE.getRedisClient().connectPubSub();
            final RedisPubSubAsyncCommands<String, String> async = this.connection.async();
            async.addListener(new RedisPubSubAdapter<String, String>() {
                @Override
                public void message(final String channel, final String message) {
                    final String[] projectBuild = message.split(":");
                    final DynamicProject project = (DynamicProject) JenkinsHelper.findTask(new ObjectId(projectBuild[0]));
                    final DynamicBuild build = project.getBuildByNumber(Integer.parseInt(projectBuild[1]));
                    build.abort();
                }
            });
            async.subscribe(CHANNEL);

        }

    }

    public void notifyCancellation(final long id) {

        if (this.connection != null) {
            RedisConnections.INSTANCE.getRedisConnection().async().publish(CHANNEL, id + "");
        }
    }
}
