package derms.net.rmulticast;

import java.time.Duration;
import java.time.Instant;

class Wait {
    static void forDuration(Duration dur) throws InterruptedException {
        Instant start = Instant.now();
        Duration elapsed;
        do {
            Thread.yield();
            elapsed = Duration.between(start, Instant.now());
        } while (elapsed.compareTo(dur) < 0);
    }
}
