package derms.replica.replica1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.logging.Logger;

public class AnnounceListener implements Runnable {
	private static final int bufsize = 1024;

	private InetSocketAddress groupAddr;
	private InetAddress localAddr;
	private Servers servers;
	private Logger log;

	public AnnounceListener(InetSocketAddress groupAddr, InetAddress localAddr, Servers servers) throws IOException {
		this.groupAddr = groupAddr;
		this.localAddr = localAddr;
		this.servers = servers;
		this.log = DermsLogger.getLogger(this.getClass());
	}

	@Override
	public void run() {
		NetworkInterface netInterface = null;
		try {
			netInterface = NetworkInterface.getByInetAddress(localAddr);
			if (netInterface == null) {
				throw new Exception("netInterface is null");
			}
		} catch (Exception e) {
			log.severe("Failed to get network interface bound to "+localAddr.toString()+": "+e.getMessage());
			return;
		}

		MulticastSocket sock = null;
		try {
			sock = new MulticastSocket(groupAddr.getPort());
			sock.joinGroup(groupAddr, netInterface);
		} catch (Exception e) {
			log.severe("Failed to open multicast socket: "+e.getMessage());
			return;
		}

		log.info("Listening to "+groupAddr.toString()+" from "+localAddr.toString()+" ("+netInterface.getName()+")");
		byte[] buf = new byte[bufsize];
		DatagramPacket pkt = new DatagramPacket(buf, buf.length);

		for (;;) {
			try {
				sock.receive(pkt);
			} catch (Exception e) {
				log.warning("Error receiving from multicast socket: "+e.getMessage());
				continue;
			}

			ObjectInputStream objStream;
			try {
				objStream = new ObjectInputStream(
					new ByteArrayInputStream(pkt.getData()));
			} catch (IOException e) {
				log.warning("Failed to create input stream: "+e.getMessage());
				continue;
			}

			City city;
			try {
				city = (City) objStream.readObject();
			} catch (Exception e) {
				log.warning("Failed to deserialize data: "+e.getMessage());
				continue;
			}

			InetAddress remote = pkt.getAddress();
			if (servers.put(city, remote) == null) {
				log.info("Added remote server "+city.toString()+" "+remote.toString());
			}
		}
	}
}