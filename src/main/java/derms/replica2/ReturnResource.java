package derms.replica2;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class ReturnResource {
	public static final int port = 5559;
	public static final int bufsize = 4096;

	static class Client {
		private CoordinatorID coordinatorID;
		private ResourceID resourceID;

		Client(CoordinatorID cid, ResourceID rid) {
			this.coordinatorID = cid;
			this.resourceID = rid;
		}

		Response sendRequest(InetAddress serverAddr) throws IOException {
			Request request = new Request(coordinatorID, resourceID);
			DatagramSocket sock;
			try {
				sock = new DatagramSocket();
			} catch (Exception e) {
				throw new IOException("Return Resource Client: failed to open socket: "+e.getMessage());
			}

			DatagramPacket requestPkt;
			try {
				requestPkt = ObjectPacket.create(request, new InetSocketAddress(serverAddr, port));
			} catch (IOException e) {
				sock.close();
				throw new IOException("Return Resource Client: failed to create request: "+e.getMessage());
			}

			sock.send(requestPkt);

			byte[] buf = new byte[bufsize];
			DatagramPacket responsePkt = new DatagramPacket(buf, buf.length);
			try {
				sock.receive(responsePkt);
			} catch (Exception e) {
				sock.close();
				throw new IOException("Return  Resource Client: error receiving from server: "+e.getMessage());
			}

			try {
				return ObjectPacket.deserialize(responsePkt, Response.class);
			} catch (IOException e) {
				throw new IOException("Return Resource Client: failed to deserialize response: "+e.getMessage());
			} finally {
				sock.close();
			}
		}
	}

	static class Server implements Runnable {
		private InetAddress localAddr;
		private Resources resources;
		private ExecutorService pool;
		private Logger log;

		Server(InetAddress localAddr, Resources resources) throws IOException {
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
					log.fine("Got request");

					Request request = null;
					try {
						request = ObjectPacket.deserialize(requestPkt, Request.class);
					} catch (IOException e) {
						log.warning("Failed to deserialize request: "+e.getMessage());
						continue;
					}

					pool.execute(new RequestHandler(request, requestPkt.getSocketAddress(), resources));
				}
			} finally {
				sock.close();
			}
		}
	}

	private static class Request implements Serializable {
		private CoordinatorID coordinatorID;
		private ResourceID resourceID;

		private Request(CoordinatorID cid, ResourceID rid) {
			this.coordinatorID = cid;
			this.resourceID = rid;
		}
	}

	private static class RequestHandler implements Runnable {
		private Request request;
		private SocketAddress client;
		private Resources resources;

		private RequestHandler(Request request, SocketAddress client, Resources resources) {
			this.request = request;
			this.client = client;
			this.resources = resources;
		}

		@Override
		public void run() {
			Response response = returnResource();

			DatagramSocket sock;
			try {
				sock= new DatagramSocket();
			} catch (Exception e) {
				System.err.println("Request Resource Server: failed to open socket: "+e.getMessage());
				return;
			}

			DatagramPacket pkt;
			try {
				pkt = ObjectPacket.create(response, client);
			} catch (IOException e) {
				System.err.println("Request Resource Server: failed to create response packet: "+e.getMessage());
				sock.close();
				return;
			}
			try {
				sock.send(pkt);
			} catch (Exception e) {
				System.err.println("Request Resource Server: failed to send response: "+e.getMessage());
			}
			sock.close();
		}

		private Response returnResource() {
			try {
				Resource resource = resources.getByID(request.resourceID);
				synchronized (resource) {
					if (!resource.isBorrowed || !resource.borrower.equals(request.coordinatorID)) {
						return new Response(Response.Status.NOT_BORROWED,
								request.resourceID+" is not borrowed by "+request.coordinatorID);
					}
					resource.isBorrowed = false;
					resource.borrower = new CoordinatorID();
					resource.borrowDuration = -1;
					return new Response(Response.Status.SUCCESS, request.coordinatorID+" successfully returned "+resource.id);
				}
			} catch (NoSuchElementException e) {
				return new Response(Response.Status.NO_SUCH_RESOURCE, "no such resource "+request.resourceID);
			}
		}
	}

	static class Response implements Serializable {
		Status status;
		String message;

		private Response(Status status, String message) {
			this.status = status;
			this.message = message;
		}

		enum Status {
			SUCCESS, NO_SUCH_RESOURCE, NOT_BORROWED
		}
	}
}
