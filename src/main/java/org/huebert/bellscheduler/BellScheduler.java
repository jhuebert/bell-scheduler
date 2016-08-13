package org.huebert.bellscheduler;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;

/**
 * Plays a bell sound based on a cron schedule contained in a file.
 */
public class BellScheduler {

    /**
     * Program information to print on each invocation.
     */
    private static final String VERSION = BellScheduler.class.getSimpleName() + " 2.0.0";

    /**
     * Schedule that defines when the cron file should be checked for changes.
     */
    private static final String UPDATE_SCHEDULE = "* * * * *";

    /**
     * Main command line program.
     *
     * @param args Command line arguments. None are expected.
     */
    public static void main(String... args) {
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
        Scheduler scheduler = new Scheduler();

        /* Create the task that plays the bell sound */
        BellPlayer bellPlayer = new BellPlayer(bellFile, loops);

        /* Create the task that schedules tasks based on the cron file */
        BellRescheduler rescheduler = new BellRescheduler(scheduler, bellPlayer, bellCronFile);
        scheduler.schedule(UPDATE_SCHEDULE, rescheduler);
        rescheduler.run();

        /* Start the cron scheduler */
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

        //TODO We could potentially copy the file to a temp directory to prevent the file server from being hit too hard

        return file;
    }

}
