package org.huebert.bellscheduler;

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

            System.out.println("Running bell - " + LocalDateTime.now());

            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            File file = Preconditions.checkNotNull((File) jobDataMap.get(BellConstants.SOUND_FILE));
            int loops = Preconditions.checkNotNull((Integer) jobDataMap.get(BellConstants.NUM_LOOPS));
            Preconditions.checkArgument(loops > 0);

            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(file));
            clip.loop(loops - 1);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
