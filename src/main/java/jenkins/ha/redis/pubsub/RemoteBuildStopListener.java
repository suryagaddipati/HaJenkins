package jenkins.ha.redis.pubsub;

import com.groupon.jenkins.dynamic.build.BuildStopListener;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.lambdaworks.redis.pubsub.RedisPubSubAdapter;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;
import hudson.Extension;
import jenkins.ha.HaExecutionAction;
import jenkins.ha.JenkinsHelper;
import jenkins.ha.redis.RedisConnections;
import org.bson.types.ObjectId;

public enum RemoteBuildStopListener {
    INSTANCE;

    public static final String CHANNEL = "jenkins:build_cancellation";
    private StatefulRedisPubSubConnection<String, String> connection;

    public void stop() {
        if (this.connection != null) this.connection.close();
    }

    public void start() {
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

    public void notifyAbort(final String buidId) {
        if (this.connection != null) {
            RedisConnections.INSTANCE.getRedisConnection().async().publish(CHANNEL, buidId);
        }

    }

    @Extension
    public static class LocalBuildStopListener implements BuildStopListener {
        @Override
        public void onStop(final DynamicBuild dynamicBuild) {
            final HaExecutionAction haExecutionAction = dynamicBuild.getAction(HaExecutionAction.class);
            if (haExecutionAction != null && !haExecutionAction.isExectionOnThisJenkinsInstance()) {
                RemoteBuildStopListener.INSTANCE.notifyAbort(dynamicBuild.getProjectId().toString() + ":" + dynamicBuild.getNumber());
            }
        }

    }


}
