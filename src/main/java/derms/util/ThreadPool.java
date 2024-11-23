package derms.util;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ThreadPool {
    public static final Duration timeout = Duration.ofSeconds(1);

    public static void shutdown(ExecutorService pool, Logger log) {
        pool.shutdown();
        try {
            // Wait for existing threads to stop.
            if (!pool.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                log.warning("Thread pool did not terminate after " + timeout + ". Forcefully shutting down...");
                pool.shutdownNow(); // Cancel running tasks.
                // Wait for tasks to stop.
                if (!pool.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS))
                    log.warning("Thread pool did not terminate after " + timeout);
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted.
            pool.shutdownNow();
            // Preserve interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    public static void shutdownNow(ExecutorService pool, Logger log) {
        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS))
                log.warning("Thread pool did not terminate after " + timeout);
        } catch (InterruptedException e) {
            log.warning("Interrupted while terminating thread pool: " + e.getMessage());
            // Preserve interrupt status.
            Thread.currentThread().interrupt();
        }
    }
}
