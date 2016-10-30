package com.groupon.jenkins.DeadlockKiller;

import jenkins.model.Jenkins;
import redis.clients.jedis.JedisPubSub;

public class RemoteQueueCancellationListener extends JedisPubSub {
    public void doRun() {
        new QueueRepository().subscribeToChannel("queue_cancellation", this);
    }

    @Override
    public void onMessage(final String channel, final String id) {
        ((RedisQueue) Jenkins.getInstance().getQueue()).cancel(Long.parseLong(id));
    }
}
