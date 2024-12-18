package derms.net.rmulticast;

import derms.net.MessagePayload;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

class ReceivedSet<T extends MessagePayload> {
    private final Map<MessageID, Entry<T>> received;

    ReceivedSet() {
        this.received = new ConcurrentHashMap<MessageID, Entry<T>>();
    }

    /**
     * Add a message to the set if it is not already present.
     *
     * @param msg The message to add to the set.
     * @return True if the set did not already contain the specified message.
     */
    boolean add(Message<T> msg) {
        return received.put(msg.id(), new Entry<T>(msg)) == null;
    }

    Message<T> getByID(MessageID mid) throws NoSuchElementException {
        Entry<T> e = received.get(mid);
        if (e == null)
            throw new NoSuchElementException("message " + mid + " not in received list.");
        return e.msg;
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
        received.remove(msg.id());
    }

    /** Retrieves, but does not remove, the oldest message, or returns null if the set is empty. */
    Message<T> peekOldest() {
        Entry<T> oldest = null;
        for (Entry<T> e : received.values())
            if (oldest == null || e.isBefore(oldest))
                oldest = e;
        if (oldest == null)
            return null;
        return oldest.msg;
    }

    Message<T> mostRecentSentBy(InetAddress member) throws NoSuchElementException {
        Entry<T> recent = null;
        for (Entry<T> e : received.values())
            if (e.msg.isSentBy(member) && (recent == null || e.isAfter(recent)))
                recent = e;
        if (recent == null)
            throw new NoSuchElementException("no message from " + member + " in received list.");
        return recent.msg;
    }

    List<Message<T>> allSentBy(InetAddress sender) {
        List<Message<T>> sent = new ArrayList<Message<T>>();
       for (Entry<T> e : received.values()) {
           if (e.msg.isSentBy(sender))
               sent.add(e.msg);
       }
       return sent;
    }

    private static class Entry<T extends MessagePayload> {
        private final Message<T> msg;
        private final Instant timestamp; // The time at which the entry was added to the set.

        private Entry(Message<T> msg) {
            this.msg = msg;
            this.timestamp = Instant.now();
        }

        private boolean isBefore(Entry<T> other) {
            return this.timestamp.isBefore(other.timestamp);
        }

        private boolean isAfter(Entry<T> other) {
            return this.timestamp.isAfter(other.timestamp);
        }

        @Override
        public int hashCode() {
            return msg.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return msg.equals(obj);
        }
    }
}
