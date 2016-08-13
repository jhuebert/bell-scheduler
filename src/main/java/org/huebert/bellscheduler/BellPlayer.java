package org.huebert.bellscheduler;

import com.google.common.base.Preconditions;

import java.io.File;
import java.time.LocalDateTime;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays the configured bell sound when run.
 */
class BellPlayer implements Runnable {

    /**
     * Path to the bell sound.
     */
    private final File file;

    /**
     * Number of times to play the bell sound
     */
    private final int loops;

    /**
     * @param file Path to the bell sound
     * @param loops Number of times to play the bell sound
     */
    BellPlayer(File file, int loops) {
        this.file = Preconditions.checkNotNull(file);
        Preconditions.checkArgument(loops > 0);
        this.loops = loops;
    }

    @Override
    public void run() {
        try {
            System.out.println("Running bell - " + LocalDateTime.now());
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(file));
            clip.loop(loops - 1);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
