package derms.replica.replica1;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

public class Announcer implements Runnable {
	public static final long intervalMillis = 3000;

	private SocketAddress group;
	private InetAddress localAddr;
	private City city;
	private Logger log;

	public Announcer(SocketAddress group, InetAddress localAddr, City city) throws IOException {
		this.group = group;
		this.localAddr = localAddr;
		this.city = city;
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
			sock = new MulticastSocket();
			sock.joinGroup(group, netInterface);
		} catch (Exception e) {
			log.severe("Failed to open multicast socket: "+e.getMessage());
			return;
		}

		log.info("Announcing from "+localAddr.toString()+" ("+netInterface.getName()+") to "+group.toString());

		DatagramPacket pkt = null;
		try {
			pkt = ObjectPacket.create(city, group);
		} catch (IOException e) {
			log.severe("Failed to create packet: "+e.getMessage());
			sock.close();
			return;
		}

		try {
			for (;;) {
				sock.send(pkt);
				Thread.sleep(intervalMillis);
			}
		} catch (IOException e) {
			log.severe("Failed to send to multicast socket "+group.toString()+": "+e.getMessage());
		} catch (InterruptedException e) {
			log.info("Interrupted.");
		} finally {
			log.info("Shutting down...");
			sock.close();
		}
	}
}