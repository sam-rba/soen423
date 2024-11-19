package derms.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;

public class ConcurrentDatagramSocket {
    private final DatagramSocket sock;

    public ConcurrentDatagramSocket() throws IOException {
        this.sock = new DatagramSocket();
    }

    public synchronized void close() {
        sock.close();
    }

    public synchronized void connect(InetAddress address, int port) {
        sock.connect(address, port);
    }

    public synchronized InetAddress getLocalAddress() {
        return sock.getLocalAddress();
    }

    public synchronized int getLocalPort() {
        return sock.getLocalPort();
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
