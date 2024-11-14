package derms.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ConcurrentMulticastSocket {
    private final MulticastSocket sock;

    /** Create a socket for sending. */
    public ConcurrentMulticastSocket() throws IOException {
        this.sock = new MulticastSocket();
    }

    /** Create a socket bound to the specified port for receiving. */
    public ConcurrentMulticastSocket(int port) throws IOException {
        this.sock = new MulticastSocket(port);
    }

    /** Join a multicast group. */
    public synchronized void joinGroup(InetAddress mcastaddr) throws IOException {
        sock.joinGroup(mcastaddr);
    }

    public synchronized void send(DatagramPacket p) throws IOException {
        sock.send(p);
    }

    public synchronized void receive(DatagramPacket p) throws IOException {
        sock.receive(p);
    }
}
