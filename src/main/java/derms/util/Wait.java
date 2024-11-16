package derms.util;

import java.time.Duration;
import java.time.Instant;

public class Wait {
    /** Yield the thread for the specified duration. */
    public static void forDuration(Duration dur) throws InterruptedException {
        Instant start = Instant.now();
        Duration elapsed;
        do {
            Thread.yield();
            if (Thread.interrupted())
                throw new InterruptedException();
            elapsed = Duration.between(start, Instant.now());
        } while (elapsed.compareTo(dur) < 0);
    }
}
