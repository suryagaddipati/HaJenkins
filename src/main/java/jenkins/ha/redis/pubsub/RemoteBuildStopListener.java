package jenkins.ha.redis.pubsub;

import com.groupon.jenkins.dynamic.build.BuildStopListener;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.Extension;
import jenkins.ha.HaExecutionAction;

@Extension
public class RemoteBuildStopListener implements BuildStopListener {
    @Override
    public void onStop(final DynamicBuild dynamicBuild) {
        final HaExecutionAction haExecutionAction = dynamicBuild.getAction(HaExecutionAction.class);
        if (haExecutionAction != null && !haExecutionAction.isExectionOnThisJenkinsInstance()) {
            RemoteLocalQueue.INSTANCE.notifyAbort(dynamicBuild.getProjectId().toString() + ":" + dynamicBuild.getNumber());
        }
    }


}
