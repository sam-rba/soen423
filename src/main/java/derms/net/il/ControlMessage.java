package derms.net.il;

import java.io.Serializable;

class ControlMessage implements Serializable {
    final Type type;
    final int id; // One greater than the sequence number of the highest sent data message.
    final int ack; // Last in-sequence data message received by the transmitter of the message.

    ControlMessage(Type type, int id, int ack) {
        this.type = type;
        this.id = id;
        this.ack = ack;
    }
}
