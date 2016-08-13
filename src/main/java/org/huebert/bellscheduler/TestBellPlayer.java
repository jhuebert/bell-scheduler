package org.huebert.bellscheduler;

import java.io.File;

public class TestBellPlayer {

    public static void main(String[] args) throws Exception {
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav",
                "/Users/jhuebert/intellij/bell-scheduler/resources/schedules/test.cron", "1");
    }

    private static void testBellScheduler() {
        testBellScheduler();
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav");
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav",
                "/Users/jhuebert/intellij/bell-scheduler/resources/schedules/bell.cron");
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav",
                "/Users/jhuebert/intellij/bell-scheduler/resources/schedules/bell.cron", "g");
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav",
                "/Users/jhuebert/intellij/bell-scheduler/resources/schedules/bell.cron", "0");
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/notthere.wav",
                "/Users/jhuebert/intellij/bell-scheduler/resources/schedules/bell.cron", "1");
        testBellScheduler("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav",
                "/Users/jhuebert/intellij/bell-scheduler/resources/schedules/notthere.cron", "1");
    }

    private static void testBellScheduler(String...args) {
        try {
            BellScheduler.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testBellPlayer() throws Exception {
        new BellPlayer(new File("/Users/jhuebert/intellij/bell-scheduler/resources/bells/1 sec bell.wav"), 5).run();
        Thread.sleep(10000);
    }

}
