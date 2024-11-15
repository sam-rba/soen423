package derms.net.rmulticast;

import derms.util.Wait;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/** If a message is not positively acknowledged after some time, Timeout puts it in the retransmissions list. */
class Timeout<T extends MessagePayload> implements Runnable {
    private static final Duration timeout = Duration.ofSeconds(1);

    private final Message<T> msg;
    private final Set<MessageID> acks; // Positively acknowledged messages.
    private final BlockingQueue<Message<T>> retransmissions;
    private final Logger log;

    /**
     * @param acks Positively acknowledged messages.
     */
    Timeout(Message<T> msg, Set<MessageID> acks, BlockingQueue<Message<T>> retransmissions) {
        this.msg = msg;
        this.acks = acks;
        this.retransmissions = retransmissions;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Wait.forDuration(timeout);
                if (acks.contains(msg.id())) {
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
}
