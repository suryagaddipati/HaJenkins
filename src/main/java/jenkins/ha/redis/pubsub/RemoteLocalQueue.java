package jenkins.ha.redis.pubsub;

import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import hudson.model.Queue;
import jenkins.ha.redis.RedisConnections;
import jenkins.ha.redis.models.RemoteQueueWaitingItem;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public enum RemoteLocalQueue {
    INSTANCE;


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

        }
        return new ArrayList<>();
    }


    public void stopCancellationListener() {

    }

    public void startCancellationListener() {

    }
}
