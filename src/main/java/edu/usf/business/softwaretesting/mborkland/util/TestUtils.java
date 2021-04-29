package edu.usf.business.softwaretesting.mborkland.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestUtils {

    public static void sleep(int seconds) {
        log.info("Sleeping {} sec during test", seconds);
        sleepMillis(seconds * 1000L);
    }

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted during sleep");
        }
    }
}
