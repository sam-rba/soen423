package derms.net.rmulticast;

import derms.net.StringMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReliableMulticastTest {
    static final InetSocketAddress group = new InetSocketAddress("225.1.2.3", 12345);

    static ReliableMulticast<StringMessage> sock;

    @BeforeAll
    static void setup() throws IOException {
        InetAddress laddr = InetAddress.getLocalHost();
        sock = new ReliableMulticast<StringMessage>(group, laddr);
    }

    @AfterAll
    static void teardown() throws IOException {
        sock.close();
    }

    // A process should receive its own messages that it sent to the group.
    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void loopback() throws IOException, InterruptedException {
        StringMessage want = new StringMessage("foo");
        sock.send(want);
        StringMessage got = sock.receive();
        assertEquals(want, got);
    }
}
