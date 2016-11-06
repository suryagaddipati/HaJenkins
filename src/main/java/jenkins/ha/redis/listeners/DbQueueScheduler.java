package jenkins.ha.redis.listeners;

import hudson.model.queue.ScheduleResult;
import jenkins.ha.JenkinsHelper;
import jenkins.ha.QueueEntry;
import jenkins.ha.RedisQueue;
import jenkins.ha.redis.QueueRepository;
import jenkins.model.Jenkins;

import java.util.logging.Logger;


public class DbQueueScheduler {

    public static final Logger LOGGER = Logger.getLogger(DbQueueScheduler.class.getName());


    public void doRun() {

        final QueueRepository queueRepository = new QueueRepository();
        final QueueEntry queueEntry = queueRepository.getNext();
        if (queueEntry != null) {
            final Jenkins jenkins = Jenkins.getInstance();
            final ScheduleResult result = ((RedisQueue) jenkins.getQueue()).scheduleFromDb(JenkinsHelper.findTask(queueEntry.getProjectId()), queueEntry.getQuitePeriod(), queueEntry.getActions());
            if (result.equals(ScheduleResult.refused())) {
//                queueRepository.save(queueEntry);
            }
        }
    }


}
