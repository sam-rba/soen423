package derms.net.tomulticast;

import derms.net.MessagePayload;
import derms.net.rmulticast.ReliableMulticast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * <p>A single-sender, multiple-receiver multicast group that enforces a total order on
 * received messages. If a process delivers message <i>A</i> before message <i>B</i>,
 * then all other processes will deliver <i>A</i> and <i>B</i> in the same order
 * (<i>A</i> before <i>B</i>).</p>
 *
 * <p>Additionally, the protocol guarantees reliable delivery of messages. If the sender
 * crashes while sending a message, either all processes will receive the message, or
 * none of them will.</p>
 *
 * <p><b>Only one sender is allowed per multicast group</b>, but a group may have multiple
 * receivers. Use {@link TotalOrderMulticastSender} to send
 * messages, and {@link TotalOrderMulticastReceiver} to receive
 * them.</p>
 */
public abstract class TotalOrderMulticast<T extends MessagePayload> {
    final ReliableMulticast<Message<T>> sock;
    protected final InetSocketAddress group;
    protected Long seq; // Sequence number.
    protected final Logger log;

    protected TotalOrderMulticast(InetSocketAddress group, InetAddress laddr) throws IOException {
        this.sock = new ReliableMulticast<Message<T>>(group, laddr);
        this.group = group;
        this.seq = (long) 0;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    /** Close the underlying socket. */
    public void close() {
        sock.close();
    }

    /** Increment the sequence number. */
    protected void incSeq() {
        if (seq < seq.MAX_VALUE) {
            seq++;
        } else {
            log.warning("Sequence number overflow. Wrapping to 0.");
            seq = (long) 0;
        }
    }
}
