package derms.replica.replica1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.SocketAddress;

public class ObjectPacket {
	public static DatagramPacket create(Serializable obj, SocketAddress remoteAddr) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(obj);
		objStream.flush();
		byte[] buf = byteStream.toByteArray();
		objStream.close();
		return new DatagramPacket(buf, buf.length, remoteAddr);
	}

	public static <E> E deserialize(DatagramPacket pkt, Class<E> clazz) throws IOException {
		ObjectInputStream objectStream;
		try {
			objectStream = new ObjectInputStream(
				new ByteArrayInputStream(pkt.getData()));
		} catch (Exception e) {
			throw new IOException("failed to create input stream: "+e.getMessage());
		}

 		try {
			return clazz.cast(objectStream.readObject());
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}