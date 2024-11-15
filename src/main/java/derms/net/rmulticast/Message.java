package derms.net.rmulticast;

import java.io.Serializable;
import java.net.InetAddress;

class Message<T extends MessagePayload> implements Serializable {
    T payload;
    InetAddress sender;
    MessageID[] acks; // IDs of messages that this message positively acknowledges.
    MessageID[] nacks; // IDs of messages that this message negatively acknowledges.

    /**
     * @param acks IDs of messages that this message positively acknowledges.
     * @param nacks IDs of messages that this message negatively acknowledges.
     */
    Message(T payload, InetAddress sender, MessageID[] acks, MessageID[] nacks) {
        this.payload = payload;
        this.sender = sender;
        this.acks = acks;
        this.nacks = nacks;
    }

    MessageID id() {
        return new MessageID(hashCode());
    }

    @Override
    public int hashCode() {
        return payload.hash();
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
}
