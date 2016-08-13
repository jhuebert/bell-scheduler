package org.huebert.bellscheduler;

import static java.util.stream.Collectors.toSet;
import static org.huebert.bellscheduler.BellConstants.PLAYER_JOB_KEY;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.utils.Key;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Performs the maintenance of the cron schedule.
 */
public class BellRescheduler implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        /* Get the current list of scheduled patterns */
        Set<String> currentPatterns = getTriggers(context);

        /* Read the updated patterns from the cron file */
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        File bellCronFile = Preconditions.checkNotNull((File) jobDataMap.get(BellConstants.CRON_FILE));
        Set<String> patterns = getPatterns(bellCronFile);

        /* Remove any scheduled patterns that no longer exist in the cron file */
        for (String pattern : Sets.difference(currentPatterns, patterns)) {
            System.out.println("Removing \"" + pattern + "\"");
            unscheduleJob(context, pattern);
        }

        /* Schedule new patterns in the cron file */
        for (String pattern : Sets.difference(patterns, currentPatterns)) {
            System.out.println("Adding \"" + pattern + "\"");
            scheduleJob(context, pattern);
        }
    }

    private static Set<String> getTriggers(JobExecutionContext context) {
        try {
            return context.getScheduler().getTriggersOfJob(PLAYER_JOB_KEY).stream()
                    .map(Trigger::getKey)
                    .map(Key::getName)
                    .collect(toSet());
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private static void unscheduleJob(JobExecutionContext context, String pattern) {
        try {
            context.getScheduler().unscheduleJob(triggerKey(pattern, BellConstants.PLAYER_GROUP));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static void scheduleJob(JobExecutionContext context, String pattern) {
        try {

            Scheduler scheduler = context.getScheduler();

            if (!scheduler.checkExists(PLAYER_JOB_KEY)) {
                JobDetail playerJob = newJob(BellPlayer.class)
                        .withIdentity(PLAYER_JOB_KEY)
                        .storeDurably(true)
                        .build();
                playerJob.getJobDataMap().putAll(context.getJobDetail().getJobDataMap());
                scheduler.addJob(playerJob, true);
            }

            Trigger trigger = newTrigger()
                    .withIdentity(pattern, BellConstants.PLAYER_GROUP)
                    .forJob(PLAYER_JOB_KEY)
                    .withSchedule(cronSchedule(pattern))
                    .build();

            scheduler.scheduleJob(trigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
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
        try {

            /* Parse the file for the patterns while ignoring comment lines */
            return Files.readAllLines(file.toPath()).stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(expression -> !expression.isEmpty())
                    .filter(expression -> !expression.startsWith("#"))
                    .collect(toSet());

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
}
