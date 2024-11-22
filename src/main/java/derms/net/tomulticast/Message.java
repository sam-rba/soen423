package derms.net.tomulticast;

import derms.net.MessagePayload;

class Message<T extends MessagePayload> implements MessagePayload, Comparable<Message<T>> {
    long seq; // Sequence number.
    T payload;

    Message(long seq, T payload) {
        this.seq = seq;
        this.payload = payload;
    }

    @Override
    public int hash() {
        return (int) seq * payload.hash();
    }

    @Override
    public int hashCode() {
        return hash();
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
        return other.seq == this.seq && other.hash() == this.hash();
    }

    @Override
    public int compareTo(Message<T> other) {
        return Long.compare(this.seq, other.seq);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + seq + ", " + payload + "}";
    }
}
