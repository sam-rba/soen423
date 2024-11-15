package derms.net.rmulticast;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ReceivedSet<T extends Serializable & Hashable> {
    private final Set<Message<T>> received;

    ReceivedSet() {
        this.received = new ConcurrentHashMap<Message<T>, Void>().keySet();
    }

    void add(Message<T> e) {
        received.add(e);
    }

    // TODO: faster search.
    boolean contains(MessageID mid) {
        for (Message<T> msg : received)
            if (msg.id().equals(mid))
                return true;
        return false;
    }
}
