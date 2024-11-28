package derms.replica.replica2;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SwapResource {
	public static final int port = 5560;
	public static final int bufSize = 4096;

	public static class Client {
		private CoordinatorID cid;
		private ResourceID oldRID;
		private ResourceID newRID;

		public Client(CoordinatorID cid, ResourceID oldRID, ResourceID newRID) {
			this.cid = cid;
			this.oldRID = oldRID;
			this.newRID = newRID;
		}

		public Response sendRequest(InetAddress serverAddr) throws IOException {
			Request request = new Request(cid, oldRID, newRID);
			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				throw new IOException("Swap Resource Client: failed to open socket: "+e.getMessage());
			}

			DatagramPacket requestPkt;
			try {
				requestPkt = ObjectPacket.create(request, new InetSocketAddress(serverAddr, port));
			} catch (IOException e) {
				sock.close();
				throw new IOException("Swap Resource Client: failed to create request: "+e.getMessage());
			}

			sock.send(requestPkt);

			byte[] buf = new byte[bufSize];
			DatagramPacket responsePkt = new DatagramPacket(buf, buf.length);
			try {
				sock.receive(responsePkt);
			} catch (Exception e) {
				sock.close();
				throw new IOException("Swap Resource Client: error receiving from server: "+e.getMessage());
			}

			try {
				return ObjectPacket.deserialize(responsePkt, Response.class);
			} catch (IOException e) {
				throw new IOException("Swap Resource Client: failed to deserialize response: "+e.getMessage());
			} finally {
				sock.close();
			}
		}
	}

	public static class Server implements Runnable {
		private InetAddress localAddr;
		private Resources resources;
		private Servers servers;
		private ExecutorService pool;
		private Logger log;

		public Server(InetAddress localAddr, Resources resources, Servers servers) throws IOException {
			this.localAddr = localAddr;
			this.resources = resources;
			this.servers = servers;
			pool = Executors.newWorkStealingPool();
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			DatagramSocket sock = null;
			try {
				sock = new DatagramSocket(port, localAddr);
			} catch (Exception e) {
				log.severe("Failed to bind socket to "+localAddr+": "+e.getMessage());
				return;
			}
			log.info("Listening on "+localAddr+":"+port);

			DatagramPacket requestPkt = new DatagramPacket(new byte[bufSize], bufSize);

			try {
				for (;;) {
					try {
						sock.receive(requestPkt);
					} catch (Exception e) {
						log.warning("Error receiving from socket: "+e.getMessage());
						continue;
					}
					log.fine("Got request");

					Request request = null;
					try {
						request = ObjectPacket.deserialize(requestPkt, Request.class);
					} catch (IOException e) {
						log.warning("Failed to deserialize request: "+e.getMessage());
						continue;
					}

					SocketAddress client = requestPkt.getSocketAddress();
					try {
						RequestHandler handler = new RequestHandler(request, client, resources, servers);
						pool.execute(handler);
					} catch (IOException e) {
						log.warning("Failed to create request handler: "+e.getMessage());
						continue;
					}
				}
			} finally {
				sock.close();
			}
		}
	}

	private static class Request implements Serializable {
		private CoordinatorID cid;
		private ResourceID oldRID;
		private ResourceID newRID;

		private Request(CoordinatorID cid, ResourceID oldRID, ResourceID newRID) {
			this.cid = cid;
			this.oldRID = oldRID;
			this.newRID = newRID;
		}
	}

	private static class RequestHandler implements Runnable {
		private Request request;
		private SocketAddress client;
		private Resources resources;
		private Servers servers;
		private Logger log;

		private RequestHandler(Request request, SocketAddress client, Resources resources, Servers servers) throws IOException {
			this.request = request;
			this.client = client;
			this.resources = resources;
			this.servers = servers;
			this.log = DermsLogger.getLogger(this.getClass());
		}

		@Override
		public void run() {
			Response response = swapResources();

			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				log.severe("failed to open socket: "+e.getMessage());
				return;
			}

			DatagramPacket pkt;
			try {
				pkt = ObjectPacket.create(response, client);
			} catch (IOException e) {
				log.severe("failed to create response: "+e.getMessage());
				sock.close();
				return;
			}

			try {
				sock.send(pkt);
			} catch (Exception e) {
				log.severe("failed to send response: "+e.getMessage());
			} finally {
				sock.close();
			}
		}

		private Response swapResources() {
			try {
				Resource resource = resources.getByID(request.oldRID);
				synchronized (resource) {
					if (!resource.borrower.equals(request.cid)) {
						return new Response(Response.Status.NOT_BORROWED, "resource "+request.oldRID+" not borrowed by "+request.cid);
					}
					try {
						acquireNewResource(resource.borrowDuration);
						returnOldResource(resource);
						return new Response(Response.Status.SUCCESS, request.cid+" success fully swapped "+request.oldRID+" for "+request.newRID);
					} catch (UnknownHostException e) {
						return new Response(Response.Status.UNKNOWN_HOST, e.getMessage());
					} catch (IOException e) {
						return new Response(Response.Status.FAILURE, e.getMessage());
					} catch (CannotBorrow e) {
						return new Response(Response.Status.CANNOT_BORROW, e.getMessage());
					}
				}
			} catch (NoSuchElementException e) {
				return new Response(Response.Status.NO_SUCH_RESOURCE, "no such resource "+request.oldRID);
			}
		}

		private void acquireNewResource(int borrowDuration) throws UnknownHostException, IOException, CannotBorrow {
			RequestResource.Client requestClient = new RequestResource.Client(request.cid, request.newRID, borrowDuration);
			City city = new City(request.newRID.city);
			InetAddress requestResourceServer = servers.get(city);
			if (requestResourceServer == null) {
				throw new UnknownHostException(city.toString());
			}
			RequestResource.Response response = requestClient.sendRequest(requestResourceServer);
			// TODO: make exception handling more granular---pass through status from Request.
			if (response.status != RequestResource.Response.Status.SUCCESS) {
				throw new CannotBorrow(request.cid, request.newRID, response.message);
			}
		}

		private void returnOldResource(Resource r) {
			r.isBorrowed = false;
			r.borrower = new CoordinatorID();
			r.borrowDuration = -1;
		}
	}

	public static class Response implements Serializable {
		public Status status;
		public String message;

		private Response(Status status, String message) {
			this.status = status;
			this.message = message;
		}

		public enum Status {
			SUCCESS,
			FAILURE,
			NO_SUCH_RESOURCE,
			NOT_BORROWED,
			CANNOT_BORROW,
			UNKNOWN_HOST
		}
	}

	private static class CannotBorrow extends Exception {
		CoordinatorID attemptedBorrower;
		ResourceID rid;
		String message;

		private CannotBorrow(CoordinatorID attemptedBorrower, ResourceID rid, String message) {
			this.attemptedBorrower = attemptedBorrower;
			this.rid = rid;
			this.message = message;
		}

		@Override
		public String getMessage() {
			return attemptedBorrower+" failed to borrow "+rid+": "+message;
		}
	}
}