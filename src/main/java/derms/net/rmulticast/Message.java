package derms.net.rmulticast;

import java.io.Serializable;
import java.net.InetAddress;

class Message<T extends Serializable & Hashable> implements Serializable {
    T payload;
    InetAddress sender;
    MessageID[] positiveAcks; // IDs of messages that this message positively acknowledges.
    MessageID[] negativeAcks; // IDs of messages that this message negatively acknowledges.

    Message(T payload, InetAddress sender, MessageID[] positiveAcks, MessageID[] negativeAcks) {
        this.payload = payload;
        this.sender = sender;
        this.positiveAcks = positiveAcks;
        this.negativeAcks = negativeAcks;
    }

    MessageID id() {
        return new MessageID(hashCode());
    }

    @Override
    public int hashCode() {
        return payload.hash();
    }
}
