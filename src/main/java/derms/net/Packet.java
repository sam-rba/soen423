package derms.net;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Packet {
    public static DatagramPacket encode(Serializable obj, SocketAddress dst) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(obj);
        objStream.flush();
        byte[] buf = byteStream.toByteArray();
        objStream.close();
        return new DatagramPacket(buf, buf.length, dst);
    }

    public static DatagramPacket encode(Serializable obj, InetAddress dstAddr, int dstPort) throws IOException {
        return encode(obj, new InetSocketAddress(dstAddr, dstPort));
    }

    public static <T extends Serializable> T decode(DatagramPacket pkt, Class<T> clazz) throws IOException, ClassNotFoundException, ClassCastException {
        ObjectInputStream objStream = new ObjectInputStream(
                new ByteArrayInputStream(pkt.getData()));
        T obj = clazz.cast(objStream.readObject());
        objStream.close();
        return obj;
    }
}
