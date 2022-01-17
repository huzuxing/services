package com.fmrt.common.guid.util;

import java.time.Instant;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/12 21:44
 */
public class TimeUtils {
    private static final long TWEPOCH = 1537459078000l;
    public static long getTime() {
        return Instant.now().toEpochMilli() - TWEPOCH;
    }
    public static void validateTimestamp(long lastTimestamp, long timestamp) {
        if ((timestamp - lastTimestamp) < 0) {
            throw new IllegalStateException(
                    String.format("time goes backford, refused to generate guid for %d millisecond", (timestamp - lastTimestamp)));
        }
    }

    public static long tillNextTime(long lastTimestamp) {
        var timestamp = getTime();
        while ((timestamp - lastTimestamp) <= 0) {
            timestamp = getTime();
        }
        return timestamp;
    }
}
