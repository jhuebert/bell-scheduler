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
     * @param file Path to the bell sound
     */
    BellPlayer(File file) {
        this.file = Preconditions.checkNotNull(file);
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
