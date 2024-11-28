package derms.replica.replica2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.logging.Logger;

public class ResourceAvailability {
	public static final int port = 5556;

	public static class Client implements Runnable {
		private InetAddress serverAddr;
		private ResourceType request;
		private Collection<Resource> resources;
		private Logger log;

		public Client(InetAddress serverAddr, ResourceType request, Collection<Resource> response) throws IOException {
			this.serverAddr = serverAddr;
			this.request = request;
			this.resources = response;
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				log.severe("Error binding socket: "+e.getMessage());
				return;
			}
			log.fine("Created socket");

			DatagramPacket reqPkt;
			try {
				reqPkt = ObjectPacket.create(request, new InetSocketAddress(serverAddr, port));
			} catch (IOException e) {
				log.severe("Error creating request: "+e.getMessage());
				sock.close();
				return;
			}

			try {
				sock.send(reqPkt);
			} catch (IOException e) {
				log.severe("Error sending request: "+e.getMessage());
				sock.close();
				return;
			}
			log.fine("Sent request");

			Resource[] response;
			try {
				response = ResourceTransfer.receive(sock);
			} catch (IOException e) {
				log.severe(e.getMessage());
				return;
			} finally {
				sock.close();
			}

			for (Resource resource : response) {
				resources.add(resource);
			}
		}
	}

	public static class Server implements Runnable {
		public static final int bufsize = 1024;

		private InetAddress localAddr;
		private Resources resources;
		private Logger log;

		public Server(InetAddress localAddr, Resources resources) throws IOException {
			this.localAddr = localAddr;
			this.resources = resources;
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			DatagramSocket sock = null;
			try {
				sock = new DatagramSocket(port, localAddr);
			} catch (Exception e) {
				log.severe("Failed to bind socket to "+localAddr.toString()+": "+e.getMessage());
				return;
			}

			log.info("Listening on "+localAddr.toString()+":"+port);

			DatagramPacket request = new DatagramPacket(new byte[bufsize], bufsize);
			try {
				for (;;) {
					try {
						sock.receive(request);
					} catch (Exception e) {
						log.warning("Error receiving from socket: "+e.getMessage());
						continue;
					}

					ResourceType requestedName = null;
					try {
						requestedName = ObjectPacket.deserialize(request, ResourceType.class);
					} catch (IOException e) {
						log.warning("Failed to deserialize request: "+e.getMessage());
						continue;
					}
					log.info("Got request: "+requestedName);

					Resource[] response = resources.getByName(requestedName);
					try {
						ResourceTransfer.send(response, request.getSocketAddress());
					} catch (IOException e) {
						log.warning("Error transfering resources: "+e.getMessage());
					}
				}
			} finally {
				sock.close();
			}
		}
	}
}
