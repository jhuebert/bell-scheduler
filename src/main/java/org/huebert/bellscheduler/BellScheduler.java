package org.huebert.bellscheduler;

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
     * Main command line program.
     *
     * @param args Command line arguments. None are expected.
     */
    public static void main(String[] args) {
        System.out.println(VERSION);

        /* Check the input arguments */
        if (args.length != 2) {
            System.err.println("ERROR: incorrect number of arguments specified");
            System.err.println("usage: BellRunner [bell wav] [bell cron]");
            return;
        }

        /* Get the file paths */
        File bellFile = getFile(args[0]);
        File bellCronFile = getFile(args[1]);

        /* Create the cron scheduler */
        Scheduler scheduler = new Scheduler();

        /* Create the task that plays the bell sound */
        BellPlayer bellPlayer = new BellPlayer(bellFile);

        /* Create the task that schedules tasks based on the cron file */
        BellRescheduler rescheduler = new BellRescheduler(scheduler, bellPlayer, bellCronFile);
        scheduler.schedule("* * * * *", rescheduler);
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
        File file = new File(path);

        /* Ensure that the file exists */
        if (!file.exists()) {
            throw new IllegalArgumentException("\"" + path + "\" does not exist");
        }

        /* Ensure that the file is a file and not a directory */
        if (!file.isFile()) {
            throw new IllegalArgumentException("\"" + path + "\" is not a file");
        }

        /* Ensure that the file can be read */
        if (!file.canRead()) {
            throw new IllegalArgumentException("\"" + path + "\" is not readable");
        }

        return file;
    }


}
