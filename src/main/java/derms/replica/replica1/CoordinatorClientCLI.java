package derms.replica.replica1;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class CoordinatorClientCLI extends CLI {
	public static final String usage = "Usage: java CoordinatorClient <city> <id number>";

	private final CoordinatorClient client;

	private CoordinatorClientCLI(City city, short idNum) throws UnknownHostException, MalformedURLException {
		client = new CoordinatorClient(city, idNum);
		System.out.println("ID: "+client.id);

		commands.put("request", new Request());
		cmdDescriptions.add(new Description(
			"request <resource ID> <duration>",
			"Borrow a resource and reduce its duration."));

		commands.put("find", new Find());
		cmdDescriptions.add(new Description(
			"find <resource name>",
			"List borrowed resources."));

		commands.put("return", new Return());
		cmdDescriptions.add(new Description(
			"return <resourceID>",
			"Return a currently borrowed resource."));

		commands.put("swap", new Swap());
		cmdDescriptions.add(new Description(
			"swap <old resourceID> <new resourceID>",
			"Return the old resource and borrow the new one."));

		argDescriptions.add(new Description(
			"<resource ID>",
			"3-letter city code followed by 4-digit number."));
		argDescriptions.add(new Description(
			"<resource name>",
			"E.g., AMBULANCE."));
		argDescriptions.add(new Description(
			"<duration>",
			"A number representing a time period."));
	}

	public static void main(String[] cmdlineArgs) {
		Args args = null;
		try {
			args = new Args(cmdlineArgs);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println(usage);
			System.exit(1);
		}

		try {
			(new CoordinatorClientCLI(args.city, args.idNum)).run();
		} catch (UnknownHostException | MalformedURLException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private class Request implements Command {
		public void exec(String[] args) {
			if (args.length < 2) {
				System.out.println("invalid arguments for 'request'");
			} else {
				requestResource(args[0], args[1]);
			}
		}
	}

	private void requestResource(String resourceIDStr, String durationStr) {
		ResourceID resourceID;
		try {
			resourceID = ResourceID.parse(resourceIDStr);
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return;
		}

		int duration;
		try {
			duration = Integer.parseInt(durationStr);
			if (duration < 0) {
				throw new IllegalArgumentException("invalid duration: "+durationStr);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		try {
			client.requestResource(resourceID, duration);
			System.out.println("Successfully borrowed resource.");
		} catch (ServerCommunicationError | NoSuchResourceException | AlreadyBorrowedException | InvalidDurationException e) {
			System.out.println("Failed to borrow resource: "+e.getMessage());
		}
	}

	private class Return implements Command {
		public void exec(String[] args) {
			if (args.length < 1) {
				System.out.println("invalid arguments for 'return'");
			} else {
				returnResource(args[0]);
			}
		}
	}

	private void returnResource(String resourceIDStr) {
		ResourceID resourceID;
		try {
			resourceID = ResourceID.parse(resourceIDStr);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid resource ID: "+e.getMessage());
			return;
		}

		try {
			client.returnResource(resourceID);
			System.out.println("Successfully returned resource "+resourceIDStr);
		} catch (ServerCommunicationError | NoSuchResourceException | NotBorrowedException e) {
			System.out.println("Failed to return resource: "+e.getMessage());
		}
	}

	private class Find implements Command {
		public void exec(String[] args) {
			if (args.length < 1) {
				System.out.println("invalid arguments for 'find'");
			} else {
				findResource(args[0]);
			}
		}
	}

	private void findResource(String resourceNameStr) {
		ResourceName resourceName;
		try {
			resourceName = ResourceName.parse(resourceNameStr);
		} catch (Exception e) {
			System.out.println("Invalid resource name: "+resourceNameStr);
			return;
		}

		Resource[] resources;
		try {
			resources = client.findResource(resourceName);
		} catch (ServerCommunicationError e) {
			System.out.println("Failed to find resource "+resourceName+": "+e.getMessage());
			return;
		}

		System.out.println("Borrowed "+resourceNameStr+" resources:");
		for (Resource r : resources) {
			String rid = r.id.toString();
			System.out.println("\t"+rid+" "+r.borrowDuration);
		}
	}

	private class Swap implements Command {
		public void exec(String[] args) {
			if (args.length < 2) {
				System.out.println("invalid arguments for 'swap'");
			} else {
				swapResource(args[0], args[1]);
			}
		}
	}

	private void swapResource(String oldRIDStr, String newRIDStr) {
		ResourceID oldRID;
		ResourceID newRID;
		try {
			oldRID = ResourceID.parse(oldRIDStr);
			newRID = ResourceID.parse(newRIDStr);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid resource id");
			return;
		}

		try {
			client.swapResource(oldRID, newRID);
			System.out.println("Successfully swapped "+oldRID+" for "+newRID);
		} catch (ServerCommunicationError | NoSuchResourceException e) {
			System.out.println("Failed to swap resources: "+e.getMessage());
		}
	}

	private static class Args {
		private final City city;
		private final short idNum;

		private Args(String[] args) throws IllegalArgumentException {
			if (args.length < 1) {
				throw new IllegalArgumentException("Missing argument 'city'");
			}
			city = new City(args[0]);

			if (args.length < 2) {
				throw new IllegalArgumentException("Missing argument 'id number'");
			}
			try {
				idNum = Short.parseShort(args[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Bad value of 'id number'");
			}
		}
	}
}
