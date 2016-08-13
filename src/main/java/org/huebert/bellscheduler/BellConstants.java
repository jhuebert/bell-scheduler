package org.huebert.bellscheduler;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

interface BellConstants {

    /* Rescheduler constants */
    String RESCHEDULER_JOB = "reschedulerJob";
    String RESCHEDULER_TRIGGER = "reschedulerTrigger";
    String RESCHEDULER_GROUP = "reschedulerGroup";
    JobKey RESCHEDULER_JOB_KEY = jobKey(RESCHEDULER_JOB, RESCHEDULER_GROUP);
    TriggerKey RESCHEDULER_TRIGGER_KEY = triggerKey(RESCHEDULER_TRIGGER, RESCHEDULER_GROUP);

    /* Player constants */
    String PLAYER_JOB = "playerJob";
    String PLAYER_GROUP = "playerGroup";
    JobKey PLAYER_JOB_KEY = jobKey(PLAYER_JOB, PLAYER_GROUP);

    /* Job data constants */
    String CRON_FILE = "bellCronFile";
    String SOUND_FILE = "bellSoundFile";
    String NUM_LOOPS = "bellLoops";

}
