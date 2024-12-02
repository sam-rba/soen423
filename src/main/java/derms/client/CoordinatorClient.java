package derms.client;

import derms.frontend.DERMSInterface;

import java.net.MalformedURLException;

public class CoordinatorClient extends CLI {
    public static final String usage = "Usage: java derms.client.CoordinatorClient <coordinator ID> <FE host>";

    private final String coordinatorID;
    private final DERMSInterface server;

    private CoordinatorClient(String coordinatorID, String FEhost) throws MalformedURLException {
        this.coordinatorID = coordinatorID;
        this.server = Client.connectToServer(FEhost);

        commands.put("request", new Request());
        cmdDescriptions.add(new Description(
                "request <resource ID> <duration>",
                "Borrow a resource."));

        commands.put("find", new Find());
        cmdDescriptions.add(new Description(
                "find <resource name>",
                "List borrowed resources."));

        commands.put("return", new Return());
        cmdDescriptions.add(new Description(
                "return <resource ID>",
                "Return a borrowed resource."));

        commands.put("swap", new Swap());
        cmdDescriptions.add(new Description(
                "swap <old resource ID> <old resource type> <new resource ID> <new resource type>",
                "Return the old resource and borrow the new one."));
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(usage);
            System.exit(1);
        }

        String coordinatorID = args[0];
        String FEhost = args[1];

        try {
            (new CoordinatorClient(coordinatorID, FEhost)).run();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class Request implements Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 2)
                System.out.println("invalid arguments for 'request'");
            else
                request(args[0], args[1]);
        }

        private void request(String resourceID, String durationStr) {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration < 0)
                    throw new NumberFormatException("duration less than 0");
                String response = server.requestResource(coordinatorID, resourceID, duration);
                System.out.println(response);
            } catch (NumberFormatException e) {
                System.out.println("invalid duration: " + e.getMessage());
            }
        }
    }

    private class Find implements Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 1)
                System.out.println("invalid arguments for 'find'");
            else find(args[0]);
        }

        private void find(String resourceID) {
            String response = server.findResource(coordinatorID, resourceID);
            System.out.println(response);
        }
    }

    private class Return implements Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 1)
                System.out.println("invalid arguments for 'return'");
            else
                returnResource(args[0]);
        }

        private void returnResource(String resourceID) {
            String response = server.returnResource(coordinatorID, resourceID);
            System.out.println(response);
        }
    }

    private class Swap implements Command {
        @Override
        public void exec(String[] args) {
            if (args.length < 4)
                System.out.println("invalid arguments for 'swap'");
            else
                swap(args[0], args[1], args[2], args[3]);
        }

        private void swap(String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
            String response = server.swapResource(coordinatorID, oldResourceID, oldResourceType, newResourceID, newResourceType);
            System.out.println(response);
        }
    }
}
