package org.huebert.bellscheduler;

import com.google.common.collect.Sets;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Performs the maintenance of the cron schedule.
 */
class BellRescheduler implements Runnable {

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
    BellRescheduler(Scheduler scheduler, BellPlayer bellPlayer, File bellCronPath) {
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
}
