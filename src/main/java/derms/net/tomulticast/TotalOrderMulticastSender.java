package derms.net.tomulticast;

import derms.net.MessagePayload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

/**
 * The single sending process in a {@link TotalOrderMulticast} group. <b>Only one sender is
 *  allowed per group.</b>
 */
public class TotalOrderMulticastSender<T extends MessagePayload> extends TotalOrderMulticast<T> {
    /**
     * Join the specified totally-ordered multicast group as its lone sender.
     *
     * @param group The IP address and port of the multicast group to join.
     * @param laddr The IP address of the local process.
     * @param ifs The network interface to use.
     */
    public TotalOrderMulticastSender(InetSocketAddress group, InetAddress laddr, NetworkInterface ifs) throws IOException {
        super(group, laddr, ifs);
    }

    /**
     * Join the specified totally-ordered multicast group as its lone sender using the
     * default network interface of the machine.
     *
     * @param group The IP address and port of the multicast group to join.
     * @param laddr The IP address of the local process.
     */
    public TotalOrderMulticastSender(InetSocketAddress group, InetAddress laddr) throws IOException {
        super(group, laddr);
    }

    /** Send a message to the group. */
    public void send(T payload) throws IOException {
        Message<T> msg = new Message<T>(seq, payload);
        sock.send(msg);
        incSeq();
        log.info("Sent " + msg + " from " + sock + " to " + group);
    }
}
