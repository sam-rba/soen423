package derms.replica.replica2;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class RequestResource {
	public static final int port = 5557;
	public static final int bufsize = 4096;

	public static class Client {
		private CoordinatorID coordinatorID;
		private ResourceID resourceID;
		private int duration;

		public Client(CoordinatorID coordinatorID, ResourceID resourceID, int duration) {
			this.coordinatorID = coordinatorID;
			this.resourceID = resourceID;
			this.duration = duration;
		}

		public Response sendRequest(InetAddress serverAddr) throws IOException {
			Request request = new Request(coordinatorID, resourceID, duration);
			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				throw new IOException("Request Resource Client: failed to open socket: "+e.getMessage());
			}

			DatagramPacket requestPkt;
			try {
				requestPkt = ObjectPacket.create(request, new InetSocketAddress(serverAddr, port));
			} catch (IOException e) {
				sock.close();
				throw new IOException("Request Resource Client: failed to create request: "+e.getMessage());
			}

			sock.send(requestPkt);

			byte[] buf = new byte[bufsize];
			DatagramPacket responsePkt = new DatagramPacket(buf, buf.length);
			try {
				sock.receive(responsePkt);
			} catch (Exception e) {
				sock.close();
				throw new IOException("Request  Resource Client: error receiving from server: "+e.getMessage());
			}

			try {
				return ObjectPacket.deserialize(responsePkt, Response.class);
			} catch (IOException e) {
				throw new IOException("Request Resource Client: failed to deserialize response: "+e.getMessage());
			} finally {
				sock.close();
			}
		}
	}

	public static class Server implements Runnable {
		private InetAddress localAddr;
		private Resources resources;
		private ExecutorService pool;
		private Logger log;

		public Server(InetAddress localAddr, Resources resources) throws IOException {
			this.localAddr = localAddr;
			this.resources = resources;
			pool = Executors.newWorkStealingPool();
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			DatagramSocket sock = null;
			try {
				sock = new DatagramSocket(port, localAddr);
			} catch (Exception e) {
				log.severe("Failed to bind socket to "+localAddr.toString()
					+": "+e.getMessage());
				return;
			}

			log.info("Listening on "+localAddr.toString()+":"+port);
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

					Request request = null;
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

	private static class Request implements Serializable {
		private CoordinatorID coordinatorID;
		private ResourceID resourceID;
		private int duration;

		private Request(CoordinatorID cid, ResourceID rid, int duration) {
			this.coordinatorID = cid;
			this.resourceID = rid;
			this.duration = duration;
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
			Response response = borrow();

			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				log.severe("Failed to open socket: "+e.getMessage());
				return;
			}

			DatagramPacket pkt;
			try {
				pkt = ObjectPacket.create(response, client);
			} catch (IOException e) {
				log.severe("Failed to create response packet: "+e.getMessage());
				sock.close();
				return;
			}
			try {
				sock.send(pkt);
			} catch (Exception e) {
				log.severe("Failed to send response: "+e.getMessage());
			}
			sock.close();
		}

		private Response borrow() {
			try {
				Resource resource = resources.getByID(request.resourceID);
				synchronized (resource) {
					if (resource.isBorrowed && !request.coordinatorID.equals(resource.borrower)) {
						return new Response(Response.Status.ALREADY_BORROWED,
								request.coordinatorID+" cannot borrow "+request.resourceID
										+"; already borrowed by "+resource.borrower);
					} else if (request.duration <= 0) {
						return new Response(Response.Status.INVALID_DURATION,
								"duration "+request.duration+" less than 1");
					} else if (request.duration > resource.duration) {
						return new Response(Response.Status.INVALID_DURATION,
								"cannot borrow "+resource.id+" for duration of "+request.duration
										+"; only "+resource.duration+" remaining");
					}

					if (resource.borrower.equals(request.coordinatorID)) {
						// Resource is already borrowed. Add to existing duration.
						resource.borrowDuration += request.duration;
					} else {
						resource.borrowDuration = request.duration;
					}
					resource.borrower = request.coordinatorID;
					resource.isBorrowed = true;
					resource.duration -= request.duration;

					return new Response(Response.Status.SUCCESS, request.coordinatorID
						+" successfully borrowed "+request.resourceID);
				}
			} catch (NoSuchElementException e) {
				return new Response(Response.Status.NO_SUCH_RESOURCE, "no such resource "+request.resourceID);
			}
		}
	}

	public static class Response implements Serializable {
		public Status status;
		public String message;

		private Response(Status status, String message) {
			this.status = status;
			this.message = message;
		}

		private Response() {
			this(Status.SUCCESS, "");
		}

		public enum Status {
			SUCCESS, NO_SUCH_RESOURCE, ALREADY_BORROWED, INVALID_DURATION
		}
	}
}

