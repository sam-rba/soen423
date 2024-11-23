package derms.replica.replica1;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class ResponderClientCLI extends CLI {
	public static final String usage = "Usage: java ResponderClientCLI <city> <id number>";

	private final ResponderClient client;

	private ResponderClientCLI(City city, short idNum) {
		client = new ResponderClient(city, idNum);
		System.out.println("ID: "+client.id);

		commands.put("add", new Add());
		cmdDescriptions.add(new Description(
			"add <resource ID> <resource name> <duration>",
			"Add a resource to the server"));

		commands.put("remove", new Remove());
		cmdDescriptions.add(new Description(
			"remove <resource ID> <duration>",
			"Decrease the duration of a resource. If duration is negative, the tresource is removed entirely."));

		commands.put("list",  new List());
		cmdDescriptions.add(new Description(
			"list <resource name>",
			"List available resources."));

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
			System.err.println(e);
			System.err.println(usage);
			System.exit(1);
		}

		(new ResponderClientCLI(args.city, args.idNum)).run();
	}

	private class Add implements Command {
		public void exec(String[] args) {
			if (args.length < 3) {
				System.out.println("invalid arguments for 'add'");
			} else {
				add(args[0], args[1], args[2]);
			}
		}
	}

	private void add(String resourceIDStr, String resourceNameStr, String durationStr) {
		ResourceID rid = ResourceID.parse(resourceIDStr);
		ResourceName name = ResourceName.parse(resourceNameStr);
		int duration = Integer.parseInt(durationStr);
		if (duration < 0) {
			throw new NumberFormatException("duration less than 0");
		}
		try {
			client.add(rid, name, duration);
			System.out.println("Successfully added resource to server.");
		} catch (Exception e) {
			System.out.println("Failed to add resource: "+e.getMessage());
		}
	}

	private class Remove implements Command {
		public void exec(String[] args) {
			if (args.length < 2) {
				System.out.println("invalid arguments for 'remove'");
			} else {
				remove(args[0], args[1]);
			}
		}
	}

	private void remove(String resourceIDStr, String durationStr) {
		try {
			ResourceID resourceID = ResourceID.parse(resourceIDStr);
			int duration = Integer.parseInt(durationStr);
			client.remove(resourceID, duration);
			System.out.println("Successfully removed resource from server.");
		} catch (NumberFormatException e) {
			System.out.println("invalid duration: "+durationStr);
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("Failed to remove resource: "+e.getMessage());
		}
	}

	private class List implements Command {
		public void exec(String[] args) {
			if (args.length < 1) {
				System.out.println("invalid arguments for 'list'");
			} else {
				list(args[0]);
			}
		}
	}

	private void list(String resourceNameStr) {
		try {
			ResourceName name = ResourceName.parse(resourceNameStr);
			Resource[] resources = client.listResources(name);
			System.out.println("Available resources:");
			for (Resource resource : resources) {
				System.out.println(resource.toString());
			}
		} catch (IllegalArgumentException e) {
			System.out.println("invalid resource name: " + resourceNameStr);
		} catch (UnknownHostException | MalformedURLException e) {
			System.err.println(e.getMessage());
		} catch (ServerCommunicationError e) {
			System.err.println("Failed to retrieve resources from server: "+e.getMessage());
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
