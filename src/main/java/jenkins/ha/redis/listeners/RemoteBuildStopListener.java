package jenkins.ha.redis.listeners;

import com.groupon.jenkins.dynamic.build.BuildStopListener;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import hudson.Extension;
import jenkins.ha.HaExecutionAction;
import jenkins.ha.JenkinsHelper;
import jenkins.ha.RedisQueue;
import jenkins.ha.redis.QueueRepository;
import jenkins.model.Jenkins;
import org.bson.types.ObjectId;
import redis.clients.jedis.JedisPubSub;

@Extension
public class RemoteBuildStopListener extends JedisPubSub implements BuildStopListener {
    @Override
    public void onStop(final DynamicBuild dynamicBuild) {
        final HaExecutionAction haExecutionAction = dynamicBuild.getAction(HaExecutionAction.class);
        if (haExecutionAction != null && !haExecutionAction.isExectionOnThisJenkinsInstance()) {
            new QueueRepository().notifyBuildAbort(dynamicBuild);
        }
    }

    @Override
    public void onMessage(final String channel, final String message) {
        final String[] projectBuild = message.split(":");
        final DynamicProject project = (DynamicProject) JenkinsHelper.findTask(new ObjectId(projectBuild[0]));
        final DynamicBuild build = project.getBuildByNumber(Integer.parseInt(projectBuild[1]));
        build.abort();
    }

    public static class RemoteQueueCancellationListener extends JedisPubSub {
        public void doRun() {
            new QueueRepository().subscribeToChannel("jenkins:queue_cancellation", this);
        }

        @Override
        public void onMessage(final String channel, final String id) {
            ((RedisQueue) Jenkins.getInstance().getQueue()).cancel(Long.parseLong(id));
        }
    }
}
