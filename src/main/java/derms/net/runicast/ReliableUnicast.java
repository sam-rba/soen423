package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/** TODO */
public class ReliableUnicast {
    public static Connection listen(InetAddress laddr, int lport) {
        // TODO
    }

    public static Connection connect(InetAddress raddr, int rport) throws IOException {
        ConcurrentDatagramSocket sock = new ConcurrentDatagramSocket();
        sock.connect(raddr, rport);
        InetAddress laddr = sock.getLocalAddress();
        int lport = sock.getLocalPort();
        Connection conn = new Connection(laddr, lport, raddr, rport, sock);

        conn.state.set(State.syncer);
        conn.start();
        while (conn.state.get() == State.syncer)
            Thread.yield();

        State state = conn.state.get();
        switch (state) {
            case established:
                return conn;
            case closed:
                conn.close();
                throw new IOException("failed to connect to " + raddr + ":" + rport);
            default:
                conn.close();
                throw new IllegalStateException("illegal connection state: " + state);
        }
    }
}
