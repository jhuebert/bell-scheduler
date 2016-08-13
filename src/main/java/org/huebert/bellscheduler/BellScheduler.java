package org.huebert.bellscheduler;

import static org.huebert.bellscheduler.BellConstants.CRON_FILE;
import static org.huebert.bellscheduler.BellConstants.NUM_LOOPS;
import static org.huebert.bellscheduler.BellConstants.RESCHEDULER_JOB_KEY;
import static org.huebert.bellscheduler.BellConstants.RESCHEDULER_TRIGGER_KEY;
import static org.huebert.bellscheduler.BellConstants.SOUND_FILE;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;

/**
 * Plays a bell sound based on a cron schedule contained in a file.
 */
public class BellScheduler {

    /**
     * Program information to print on each invocation.
     */
    private static final String VERSION = BellScheduler.class.getSimpleName() + " 2.0.1";

    /**
     * Schedule that defines when the cron file should be checked for changes.
     */
    private static final int UPDATE_SCHEDULE_SECONDS = 60;

    /**
     * Main command line program.
     *
     * @param args Command line arguments. None are expected.
     */
    public static void main(String... args) throws SchedulerException, InterruptedException {
        System.out.println(VERSION);

        /* Check the input arguments */
        Preconditions.checkArgument(args.length == 3, "Incorrect number of arguments specified.\n"
                + "Usage: BellRunner [bell wav] [bell cron] [number of bell loops]");

        /* Get the file paths */
        File bellFile = getFile(args[0]);
        File bellCronFile = getFile(args[1]);

        /* Get the number of times to play the sound file in succession */
        Integer loops = Ints.tryParse(args[2]);
        Preconditions.checkNotNull(loops, "Number of bell loops is not a number");
        Preconditions.checkArgument(loops > 0, "Number of bell loops must be at least 1");

        /* Create the cron scheduler */
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        /* Create the job that schedules tasks based on the cron file */
        JobDetail reschedulerJob = newJob(BellRescheduler.class)
                .withIdentity(RESCHEDULER_JOB_KEY)
                .build();
        reschedulerJob.getJobDataMap().put(CRON_FILE, bellCronFile);
        reschedulerJob.getJobDataMap().put(SOUND_FILE, bellFile);
        reschedulerJob.getJobDataMap().put(NUM_LOOPS, loops);

        /* Get the number of seconds between schedule updates */
        Integer scheduleUpdateSeconds =
                Ints.tryParse(System.getProperty("bellScheduler.scheduleUpdateSeconds", String.valueOf(UPDATE_SCHEDULE_SECONDS)));
        if (scheduleUpdateSeconds == null) {
            scheduleUpdateSeconds = UPDATE_SCHEDULE_SECONDS;
        }

        /* Create the trigger to check for schedule updates */
        Trigger reschedulerTrigger = newTrigger()
                .withIdentity(RESCHEDULER_TRIGGER_KEY)
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(scheduleUpdateSeconds)
                        .repeatForever())
                .build();

        /* Add the reschedule job/trigger */
        scheduler.scheduleJob(reschedulerJob, reschedulerTrigger);

        /* Start the scheduler */
        scheduler.start();
    }

    /**
     * Gets a file for the input path and checks to make sure it can be read.
     *
     * @param path Path to check and convert
     * @return File
     */
    private static File getFile(String path) {
        File file = new File(Preconditions.checkNotNull(path, "Path is null"));

        /* Ensure that the file exists */
        Preconditions.checkArgument(file.exists(), "\"" + path + "\" does not exist");

        /* Ensure that the file is a file and not a directory */
        Preconditions.checkArgument(file.isFile(), "\"" + path + "\" is not a file");

        /* Ensure that the file can be read */
        Preconditions.checkArgument(file.canRead(), "\"" + path + "\" is not readable");

        //TODO We could potentially copy the bell file to a temp directory to prevent the file server from being hit too hard

        return file;
    }

}
