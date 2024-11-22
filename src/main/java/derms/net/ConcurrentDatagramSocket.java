package derms.net;

import java.io.IOException;
import java.net.*;
import java.time.Duration;

public class ConcurrentDatagramSocket {
    private final DatagramSocket sock;

    /** Create a socket bound to any available port on the local machine. */
    public ConcurrentDatagramSocket() throws IOException {
        this.sock = new DatagramSocket();
    }

    /**
     * Creates a socket bound to the specified address and port.
     *
     * @param laddr The local IP address and port to listen on.
     */
    public ConcurrentDatagramSocket(SocketAddress laddr) throws SocketException {
        this.sock = new DatagramSocket(laddr);
    }

    public synchronized void close() {
        sock.close();
    }

    public synchronized void connect(InetAddress address, int port) {
        sock.connect(address, port);
    }

    public synchronized void connect(InetSocketAddress addr) {
        connect(addr.getAddress(), addr.getPort());
    }

    public synchronized InetAddress getLocalAddress() {
        return sock.getLocalAddress();
    }

    public synchronized int getLocalPort() {
        return sock.getLocalPort();
    }

    public synchronized SocketAddress getRemoteSocketAddress() {
        return sock.getRemoteSocketAddress();
    }

    public synchronized void receive(DatagramPacket p) throws IOException {
        sock.receive(p);
    }

    public synchronized void send(DatagramPacket p) throws IOException {
        sock.send(p);
    }

    public synchronized void setSoTimeout(Duration timeout) throws SocketException {
        sock.setSoTimeout((int) timeout.toMillis());
    }
}
