package org.huebert.bellscheduler;

import static org.huebert.bellscheduler.BellConstants.NUM_LOOPS;
import static org.huebert.bellscheduler.BellConstants.SOUND_FILE;

import com.google.common.base.Preconditions;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.time.LocalDateTime;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays the configured bell sound when run.
 */
public class BellPlayer implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {

            /* Output a message indicating when the bell is being run */
            System.out.println("Running bell - " + LocalDateTime.now());

            /* Get the bell wav file */
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            File file = Preconditions.checkNotNull((File) jobDataMap.get(SOUND_FILE));

            /* Get the number of times the bell should be run */
            int loops = Preconditions.checkNotNull((Integer) jobDataMap.get(NUM_LOOPS));
            Preconditions.checkArgument(loops > 0);

            /* Play the bell sound for the correct number of times */
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(file));
            clip.loop(loops - 1);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
