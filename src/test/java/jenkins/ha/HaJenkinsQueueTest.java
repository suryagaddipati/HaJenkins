package jenkins.ha;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class HaJenkinsQueueTest {
//    @Test
//    public void test_queue() throws InterruptedException {
//        final Jedis jedis = new Jedis("localhost");
//        final ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.submit(new Runnable() {
//            @Override
//            public void run() {
//
//                while (true) {
//                    final Jedis jedis1 = new Jedis("localhost");
//                    System.out.println(jedis1.blpop(0, "queue"));
//                }
//            }
//        });
//        jedis.rpush("queue", "Value 1");
//
//
//        Thread.sleep(4000);
//
//    }

    @Test
    public void proxy_test() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        final RemoteQueueWaitingItem target = new RemoteQueueWaitingItem(null);
//        final Queue.Item item = RemoteQueueWaitingItem.getQueueItem(target);
//        System.out.println(item);

    }

    @Test
    public void lettuce_test() throws InterruptedException {
        final RedisClient client = RedisClient.create("redis://localhost");
        final StatefulRedisConnection<String, String> connection = client.connect();
        connection.sync().lpush("meow", "purr");
        System.out.print(connection.sync().lpop("meow"));
//        connection.sync().set("key", "meow");
//        final RedisStringReactiveCommands<String, String> commands = client.connect().reactive();
//        comma
//                .get("key")
//                .subscribe(value -> System.out.println(value));
//        System.out.print(connection.sync().get("key"));
//        Thread.sleep(4000);

    }

}
