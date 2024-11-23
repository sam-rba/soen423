package derms.net.tomulticast;

import derms.net.StringMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TotalOrderMulticastTest {
    static final InetSocketAddress group = new InetSocketAddress("225.1.2.3", 12345);

    static InetAddress laddr;

    @BeforeAll
    static void setup() throws UnknownHostException {
        laddr = InetAddress.getLocalHost();
    }

    @Test
    void sendRecv1() throws IOException, InterruptedException {
        TotalOrderMulticastSender<StringMessage> sender = new TotalOrderMulticastSender<StringMessage>(group, laddr);
        TotalOrderMulticastReceiver<StringMessage> receiver = new TotalOrderMulticastReceiver<StringMessage>(group, laddr);

        StringMessage want = new StringMessage("foo");
        sender.send(want);
        StringMessage got = receiver.receive();
        assertEquals(want, got);

        receiver.close();
        sender.close();
    }
}
