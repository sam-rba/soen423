package derms.replica.replica2;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

class ResourceTransfer {
	static final int bufsize = 1024;

	static void send(Resource[] resources, SocketAddress remoteAddr) throws IOException {
		DatagramSocket sock = new DatagramSocket();

		for (Resource resource : resources) {
			DatagramPacket pkt = ObjectPacket.create(resource, remoteAddr);
			sock.send(pkt);
		}

		DatagramPacket pkt = ObjectPacket.create(new EndOfTransmission(), remoteAddr);
		sock.send(pkt);
		sock.close();
	}

	static Resource[] receive(DatagramSocket sock) throws IOException {
		List<Resource> resources = new ArrayList<Resource>();
		byte[] buf = new byte[bufsize];
		DatagramPacket response = new DatagramPacket(buf, buf.length);

		for (;;) {
			sock.receive(response);

			Object obj = ObjectPacket.deserialize(response, Object.class);
			if (obj.getClass() == EndOfTransmission.class) {
				break;
			}
			try {
				resources.add((Resource) obj);
			} catch (Exception e) {
				throw new IOException("expected Resource; got "+obj.getClass().toString());
			}
		}
		Resource[] arr = new Resource[0];
		return resources.toArray(arr);
	}

	private static class EndOfTransmission implements Serializable {}
}