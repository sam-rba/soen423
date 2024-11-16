package derms.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.time.Duration;

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

    public synchronized void setSoTimeout(Duration timeout) throws SocketException {
        sock.setSoTimeout((int) timeout.toMillis());
    }

    public synchronized void close() {
        sock.close();
    }

    @Override
    public String toString() {
        return sock.getLocalSocketAddress().toString();
    }
}
