package derms.replica1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPUtilities {
    public static void sendMessage(String message, String host, int port) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);

        byte[] buffer = message.getBytes();
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);

        socket.send(request);
        socket.close();
    }
}
