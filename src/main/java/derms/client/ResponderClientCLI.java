package derms.client;

import derms.frontend.DERMSInterface;
import derms.util.TestLogger;

import java.net.MalformedURLException;

public class ResponderClientCLI extends CLI {
    public static final String usage = "Usage: java derms.client.ResponderClient <FE host>";

    private final DERMSInterface server;

    private ResponderClientCLI(String FEhost) throws MalformedURLException {
        server = Client.connectToServer(FEhost);

        commands.put("add", new Add());
        cmdDescriptions.add(new CLI.Description(
                "add <resource ID> <resource type> <duration>",
                "Add ad resource to the server"));

        commands.put("remove", new Remove());
        cmdDescriptions.add(new CLI.Description(
                "remove <resource ID> <duration>",
                "Decrease the duration of a resource. If duration is negative, the resource is removed entirely."));

        commands.put("list", new List());
        cmdDescriptions.add(new CLI.Description(
                "list <resource name>",
                "List available resources"));
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(usage);
            System.exit(1);
        }

        String FEhost = args[0];

        try {
            (new ResponderClientCLI(FEhost)).run();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class Add implements CLI.Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 3)
                System.out.println("invalid arguments for 'add'");
            else
                add(args[0], args[1], args[2]);
        }

        private void add(String resourceID, String resourceName, String durationStr) {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration < 0) {
                    throw new NumberFormatException("duration less than 0");
                }
                String response = server.addResource(resourceID, resourceName, duration);
                System.out.println(response);
            } catch (NumberFormatException e) {
                System.out.println("invalid duration: " + durationStr);
            }
        }
    }

    private class Remove implements CLI.Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 2)
                System.out.println("invalid arguments for 'remove'");
            else
                remove(args[0], args[1]);
        }

        private void remove(String resourceID, String durationStr) {
            try {
                int duration = Integer.parseInt(durationStr);
                String response = server.removeResource(resourceID, duration);
                System.out.println(response);
            } catch (NumberFormatException e) {
                System.out.println("invalid duration: " + durationStr);
            }
        }
    }

    private class List implements CLI.Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 1)
                System.out.println("invalid arguments for 'list'");
            else
                list(args[0]);
        }

        private void list(String resourceName) {
            String response = server.listResourceAvailability(resourceName);
            System.out.println(response);
        }
    }
}
