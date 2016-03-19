package org.huebert.bellscheduler;

import com.google.common.collect.Sets;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays a bell sound based on a cron schedule contained in a file.
 */
public class BellScheduler {

    /**
     * Program information to print on each invocation.
     */
    private static final String VERSION = BellScheduler.class.getSimpleName() + " 1.0.1";

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
        Rescheduler rescheduler = new Rescheduler(scheduler, bellPlayer, bellCronFile);
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

    /**
     * Reads the cron patterns from the schedule file.
     *
     * @param file Path to the cron file
     * @return Set of patterns in the cron file
     */
    private static Set<String> getPatterns(File file) {

        /* Read all of the lines in the cron file. Return an empty set of patterns if the file can't be read. */
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            return Collections.emptySet();
        }

        /* Parse the file for the patterns while ignoring comment lines */
        Set<String> patterns = new HashSet<>();
        for (String line : lines) {
            String expression = line.trim();
            if (!expression.isEmpty() && !expression.startsWith("#")) {
                patterns.add(expression);
            }
        }

        return patterns;
    }

    /**
     * Plays the configured bell sound when run.
     */
    private static class BellPlayer implements Runnable {

        /**
         * Path to the bell sound.
         */
        private final File file;

        /**
         * @param file Path to the bell sound
         */
        public BellPlayer(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                System.out.println("Running bell - " + LocalDateTime.now());
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(file));
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Performs the maintenance of the cron schedule.
     */
    private static class Rescheduler implements Runnable {

        /**
         * Path to the cron file.
         */
        private final File bellCronPath;

        /**
         * Task that plays the bell sound.
         */
        private final BellPlayer bellPlayer;

        /**
         * Currently scheduled patterns
         */
        private final Map<String, String> scheduledPatterns = new HashMap<>();

        /**
         * Scheduler to use for adding and removing cron patterns.
         */
        private final Scheduler scheduler;

        /**
         * @param scheduler Scheduler to use for adding and removing cron patterns
         * @param bellPlayer Task that plays the bell sound
         * @param bellCronPath Path to the cron file
         */
        private Rescheduler(Scheduler scheduler, BellPlayer bellPlayer, File bellCronPath) {
            this.scheduler = scheduler;
            this.bellPlayer = bellPlayer;
            this.bellCronPath = bellCronPath;
        }

        @Override
        public void run() {

            /* Read the patterns from the cron file */
            Set<String> patterns = getPatterns(bellCronPath);

            /* Make a copy of the currently registered patterns */
            Set<String> current = new HashSet<>(scheduledPatterns.keySet());

            /* Remove any registered patterns that no longer exist in the cron file */
            for (String pattern : Sets.difference(current, patterns)) {
                System.out.println("Removing \"" + pattern + "\"");
                scheduler.deschedule(scheduledPatterns.remove(pattern));
            }

            /* Add new patterns in the cron file */
            for (String pattern : Sets.difference(patterns, current)) {
                System.out.println("Adding \"" + pattern + "\"");
                scheduledPatterns.put(pattern, scheduler.schedule(pattern, bellPlayer));
            }
        }
    }

}
