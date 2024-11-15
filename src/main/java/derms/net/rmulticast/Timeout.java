package derms.net.rmulticast;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/** If a message is not positively acknowledged after some time, Timeout puts it in the retransmissions list. */
class Timeout<T extends Serializable & Hashable> implements Runnable {
    private static final Duration timeout = Duration.ofSeconds(1);

    private final Message<T> msg;
    private final Set<MessageID> positiveAcks;
    private final BlockingQueue<Message<T>> retransmissions;
    private final Logger log;

    Timeout(Message<T> msg, Set<MessageID> positiveAcks, BlockingQueue<Message<T>> retransmissions) {
        this.msg = msg;
        this.positiveAcks = positiveAcks;
        this.retransmissions = retransmissions;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (;;) {
                waitUntilTimeout();
                if (positiveAcks.contains(msg.id())) {
                    log.info("Message " + msg.id() + "positively ack'ed.");
                    return;
                } else {
                    log.info("Message " + msg.id() + " not ack'ed after " + timeout + "; retransmitting.");
                    retransmissions.put(msg);
                }
            }
        } catch (InterruptedException e) {
            log.info("Timeout thread interrupted: " + e.getMessage());
        }
    }

    private void waitUntilTimeout() {
        Instant start = Instant.now();
        Duration elapsed;
        do {
            Thread.yield();
            elapsed = Duration.between(start, Instant.now());
        } while (elapsed.compareTo(timeout) < 0);
    }
}
