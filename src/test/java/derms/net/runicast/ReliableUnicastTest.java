package derms.net.runicast;

import derms.net.StringMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReliableUnicastTest {
    static final int port = 12345;

    static InetAddress laddr;

    @BeforeAll
    static void setup() throws UnknownHostException {
        laddr = InetAddress.getLocalHost();
    }

    @Test
    void openClose() throws IOException, InterruptedException {
        ReliableUnicastSender<StringMessage> sender = new ReliableUnicastSender<StringMessage>(new InetSocketAddress(laddr, port));
        ReliableUnicastReceiver<StringMessage> receiver = new ReliableUnicastReceiver<StringMessage>(new InetSocketAddress(laddr, port));
        sender.close();
        receiver.close();
    }

    @Test
    void sendRecv() throws IOException, InterruptedException {
        ReliableUnicastSender<StringMessage> sender = new ReliableUnicastSender<StringMessage>(new InetSocketAddress(laddr, port));
        ReliableUnicastReceiver<StringMessage> receiver = new ReliableUnicastReceiver<StringMessage>(new InetSocketAddress(laddr, port));

        StringMessage want = new StringMessage("foo");
        sender.send(want);
        StringMessage got = receiver.receive();
        assertEquals(want, got);

        sender.close();
        receiver.close();
    }
}
