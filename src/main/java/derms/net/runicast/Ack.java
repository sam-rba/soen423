package derms.net.runicast;

import java.io.Serializable;

/** A message acknowledgement. */
class Ack implements Serializable {
    final long seq; // The sequence number of the acknowledged message.

    Ack(long seq) {
        this.seq = seq;
    }
}
