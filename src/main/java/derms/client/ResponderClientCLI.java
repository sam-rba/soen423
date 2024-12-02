package derms.client;

import java.net.MalformedURLException;

public class ResponderClientCLI extends CLI {
    public static final String usage = "Usage: java derms.client.ResponderClientCLI <FE host>";

    private final ResponderClient client;

    private ResponderClientCLI(String FEhost) throws MalformedURLException {
        client = new ResponderClient(FEhost);

        commands.put("add", new Add());
        cmdDescriptions.add(new Description(
                "add <resource ID> <resource type> <duration>",
                "Add ad resource to the server"));
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing argument 'FE host'");
            System.exit(1);
        }

        String FEhost = args[0];

        try {
            (new ResponderClientCLI(FEhost)).run();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class Add implements Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 3) {
                System.out.println("invalid arguments for 'add'");
            } else {
                add(args[0], args[1], args[2]);
            }
        }

        private void add(String resourceID, String resourceName, String durationStr) {
            int duration = Integer.parseInt(durationStr);
            if (duration < 0) {
                throw new NumberFormatException("duration less than 0");
            }
            String response = client.addResource(resourceID, resourceName, duration);
            System.out.println(response);
        }
    }
}
