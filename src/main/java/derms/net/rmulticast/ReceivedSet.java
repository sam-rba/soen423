package derms.net.rmulticast;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class ReceivedSet<T extends Serializable & Hashable> {
    private final Queue<Message<T>> received;

    ReceivedSet() {
        this.received = new ConcurrentLinkedQueue<Message<T>>();
    }

    void add(Message<T> e) {
        received.add(e);
    }

    // TODO: faster search.
    Message<T> getByID(MessageID mid) throws NoSuchElementException {
        for (Message<T> msg : received)
            if (msg.id().equals(mid))
                return msg;
        throw new NoSuchElementException("message " + mid + " not in received list.");
    }

    boolean contains(MessageID mid) {
        try {
            Message<T> msg = getByID(mid);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /** Remove the specified message from the set, if it is present. */
    void remove(Message<T> msg) {
        received.remove(msg);
    }

    /** Retrieves, but does not remove, the oldest message, or returns null if the set is empty. */
    Message<T> peekOldest() {
        return received.peek();
    }

    Message<T> mostRecentSentBy(InetAddress member) throws NoSuchElementException {
        Message<T> recent = null;
        for (Message<T> msg : received) {
            if (msg.sender.equals(member))
                recent = msg;
        }
        if (recent == null)
            throw new NoSuchElementException("no message from " + member + " in received list.");
        return recent;
    }

    List<Message<T>> allSentBy(InetAddress sender) {
        List<Message<T>> sent = new ArrayList<Message<T>>();
       for (Message<T> msg : received) {
           if (msg.sender.equals(sender))
               sent.add(msg);
       }
       return sent;
    }
}
