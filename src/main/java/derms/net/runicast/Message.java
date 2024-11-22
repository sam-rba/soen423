package derms.net.runicast;

import derms.net.MessagePayload;

import java.io.Serializable;
import java.net.InetAddress;

class Message<T extends MessagePayload> implements Serializable {
    final long seq; // Sequence number.
    final T payload;

    Message(long seq, T payload) {
        this.seq = seq;
        this.payload = payload;
    }

    @Override
    public int hashCode() { return (int) seq * payload.hash(); }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Message<?> other = (Message<?>) obj;
        if (other.payload.getClass() != this.payload.getClass())
            return false;
        return other.seq == this.seq && other.payload.equals(this.payload);
    }

    @Override
    public String toString() { return getClass().getSimpleName() + "{" + seq + ", " + payload + "}"; }
}
