package com.groupon.jenkins.DeadlockKiller;

import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisQueueTest {
    @Test
    public void test_queue() throws InterruptedException {
        final Jedis jedis = new Jedis("localhost");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    final Jedis jedis1 = new Jedis("localhost");
                    System.out.println(jedis1.blpop(0, "queue"));
                }
            }
        });
        jedis.rpush("queue", "Value 1");


        Thread.sleep(4000);

    }

    @Test
    public void proxy_test() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        final RemoteQueueWaitingItem target = new RemoteQueueWaitingItem(null);
//        final Queue.Item item = RemoteQueueWaitingItem.getQueueItem(target);
//        System.out.println(item);
    }
}