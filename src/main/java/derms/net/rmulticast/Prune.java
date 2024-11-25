package derms.net.rmulticast;

import derms.net.MessagePayload;

import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

/** Free memory from the received list. */
class Prune<T extends MessagePayload> implements Runnable {
    private static final Duration period = Duration.ofMinutes(1);

    private final ReceivedSet<T> received;
    private final Set<InetAddress> groupMembers;
    private final Logger log;

    Prune(ReceivedSet<T> received, Set<InetAddress> groupMembers) {
        this.received = received;
        this.groupMembers = groupMembers;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Thread.sleep(period.toMillis());
                prune();
            }
        } catch (InterruptedException e) {
            log.info("Interrupted. Shutting down.");
        }
    }

    private void prune() {
        // Get the candidate for removal.
        Message<T> a = received.peekOldest();
        if (a == null)
            return;

        // Ensure all group members have received and acknowledge message a.
        for (InetAddress member : groupMembers) {
            try {
                Message<T> c = received.mostRecentSentBy(member);
                if (!receivedByMemberAtTimeOfSending(a, member, c)) {
                    log.info(member.toString() + " has not received " + a + "; cannot prune it.");
                    return; // Member has not received a -- cannot prune it.
                }
            } catch (NoSuchElementException e) {
                log.warning("No message received from " + member);
                return;
            }
        }

        // All group members have received and acknowledged message a. It is safe to delete.
        received.remove(a);
        log.info("Removed " + a + " from received list.");
    }

    /**
     * Return true if member has received and ack'ed message a at the time they sent message c.
     * See Observable Predicate for Delivery (OPD) in "Broadcast Protocols for Distributed Systems" Melliar-Smith et. al. (1990).
     *
     * @param a      A message that may or may not have been received by member.
     * @param member The process that sent c.
     * @param c      A message that was sent by member.
     */
    private boolean receivedByMemberAtTimeOfSending(Message<T> a, InetAddress member, Message<T> c) {
        List<Message<T>> seq = new ArrayList<Message<T>>();
        seq.add(c);
        return OPDseq(seq, a);
    }

    /**
     * Try to extend the OPD sequence to contain a.
     * See Observable Predicate for Delivery (OPD) in "Broadcast Protocols for Distributed Systems" Melliar-Smith et. al. (1990).
     *
     * @param seq A sequence of messages each of which acknowledges its predecessor by the OPD.
     * @return True if the sequence can extend to a.
     */
    private boolean OPDseq(List<Message<T>> seq, Message<T> a) {
        if (seq.contains(a))
            return true;

        // c -> ... -> b -> ... ?-> a
        Message<T> c = seq.get(0);
        Message<T> b = seq.get(seq.size() - 1);

        // All messages sent by the sender of b (that are not already in the sequence) can potentially by added to the sequence.
        List<Message<T>> potentialPredecessors = received.allSentBy(b.sender);
        potentialPredecessors.removeAll(seq);

        // Add messages that b positively acknowledged.
        for (MessageID mid : b.acks) {
            try {
                Message<T> msg = received.getByID(mid);
                potentialPredecessors.add(msg);
            } catch (NoSuchElementException e) {
                log.warning("message " + mid + ", acknowledged by " +b.id() + ", is not in the received list. Continuing anyway.");
            }
        }

        // Remove messages that c negatively acknowledged.
        for (MessageID mid : c.nacks) {
            for (int i = 0; i < potentialPredecessors.size(); i++) {
                Message<T> msg = potentialPredecessors.get(i);
                if (msg.id().equals(mid)) {
                    potentialPredecessors.remove(i);
                    break;
                }
            }
        }

        // Try and extend the sequence.
        for (Message<T> predecessor : potentialPredecessors) {
            seq.add(predecessor);
            if (OPDseq(seq, a))
                return true;
            // Failed; remove the predecessor and try another.
            seq.remove(seq.size() - 1);
        }

        // The sequence cannot be extended to include a.
        return false;
    }
}
