package derms.net.rmulticast;

import java.io.Serializable;
import java.net.InetAddress;

class Message<T extends MessagePayload> implements Serializable {
    /* Used as a salt in the hash function. Allows different messages to have
     * different IDs, even if they have the same payload. */
    private static int seq = 1;

    T payload;
    int salt; // Used in hash function. Allows messages to have unique IDs, even if they have the same payload.
    InetAddress sender;
    MessageID[] acks; // IDs of messages that this message positively acknowledges.
    MessageID[] nacks; // IDs of messages that this message negatively acknowledges.

    /**
     * @param acks IDs of messages that this message positively acknowledges.
     * @param nacks IDs of messages that this message negatively acknowledges.
     */
    Message(T payload, InetAddress sender, MessageID[] acks, MessageID[] nacks) {
        this.payload = payload;
        this.salt = seq++;
        this.sender = sender;
        this.acks = acks;
        this.nacks = nacks;
    }

    MessageID id() {
        return new MessageID(hashCode());
    }

    @Override
    public int hashCode() {
        return payload.hash() * salt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Message<?> other = (Message<?>) obj;
        if (other.payload.getClass() != this.payload.getClass())
            return false;
        return other.id().equals(this.id());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + id() + ", " + payload + "}";
    }
}
