package derms.replica2;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class FindResource {
	static final int port = 5558;

	static class Client implements Runnable {
		private InetAddress serverAddr;
		private Request request;
		private Collection<Resource> response;
		private Logger log;

		Client(Request request, InetAddress serverAddr, Collection<Resource> response) throws IOException {
			this.serverAddr = serverAddr;
			this.request = request;
			this.response = response;
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				log.severe("Failed to open socket: "+e.getMessage());
				return;
			}

			DatagramPacket requestPkt;
			try {
				requestPkt = ObjectPacket.create(request, new InetSocketAddress(serverAddr, port));
			} catch (IOException e) {
				log.severe("Failed to create request packet: "+e.getMessage());
				sock.close();
				return;
			}

			try {
				sock.send(requestPkt);
			} catch (Exception e) {
				log.severe("Failed to send request: "+e.getMessage());
				sock.close();
				return;
			}

			Resource[] resources;
			try {
				resources = ResourceTransfer.receive(sock);
			} catch (IOException e) {
				log.severe(e.getMessage());
				return;
			} finally {
				sock.close();
			}

			for (Resource r : resources) {
				response.add(r);
			}
		}
	}

	static class Server implements Runnable {
		private static final int bufsize = 4096;

		private InetAddress localAddr;
		private Resources resources;
		private ExecutorService pool;
		private Logger log;

		Server(InetAddress localAddr, Resources resources) throws IOException {
			this.localAddr = localAddr;
			this.resources = resources;
			this.pool = Executors.newWorkStealingPool();
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			DatagramSocket sock = null;
			try {
				sock = new DatagramSocket(port, localAddr);
			} catch (Exception e) {
				log.severe("Failed to bind socket: "+e.getMessage());
				return;
			}

			log.info("Running on "+localAddr.toString()+":"+port);

			DatagramPacket requestPkt = new DatagramPacket(new byte[bufsize], bufsize);
			try {
				for (;;) {
					try {
						sock.receive(requestPkt);
					} catch (Exception e) {
						log.warning("Error receiving from socket: "+e.getMessage());
						continue;
					}
					log.info("Got request");

					Request request;
					try {
						request = ObjectPacket.deserialize(requestPkt, Request.class);
					} catch (IOException e) {
						log.warning("Failed to deserialize request: "+e.getMessage());
						continue;
					}

					pool.execute(new RequestHandler(request, requestPkt.getSocketAddress(), resources, log));
				}
			} finally {
				sock.close();
			}
		}
	}

	static class Request implements Serializable {
		private CoordinatorID cid;
		private ResourceType rname;

		Request(CoordinatorID cid, ResourceType rname) {
			this.cid = cid;
			this.rname = rname;
		}
	}

	private static class RequestHandler implements Runnable {
		private Request request;
		private SocketAddress client;
		private Resources resources;
		private Logger log;

		private RequestHandler(Request request, SocketAddress client, Resources resources, Logger log) {
			this.request = request;
			this.client = client;
			this.resources = resources;
			this.log = log;
		}

		@Override
		public void run() {
			List<Resource> borrowedResources = resources.borrowed(request.cid, request.rname);
			log.info(""+borrowedResources.size()+" "+request.rname+" resources borrowed by "+request.cid);
			try {
				Resource[] arr = new Resource[0];
				ResourceTransfer.send(borrowedResources.toArray(arr), client);
			} catch (IOException e) {
				log.severe("Failed to send response: "+e.getMessage());
			}
		}
	}
}