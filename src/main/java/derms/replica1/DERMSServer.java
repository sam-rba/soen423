package derms.replica1;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@WebService(endpointInterface = "derms.replica1.DERMSInterface")
public class DERMSServer implements DERMSInterface {
    private HashMap<String, Resource> resources;
    private String serverID;
    private AtomicInteger serverPort = new AtomicInteger();
    private ConcurrentSkipListSet<String> requests = new ConcurrentSkipListSet<>();

    private static List<String> cities = Arrays.asList("MTL", "QUE", "SHE");
    private static List<String> resourceNames = Arrays.asList("AMBULANCE", "FIRETRUCK", "PERSONNEL");
    private final Random r = new Random();
    private ConcurrentHashMap<String, Integer> portsMap = null;

    public DERMSServer() {
        // Default constructor to support JAX-WS
//        super();
//        this.serverID = "MTL";
//        resources = new HashMap<>();
//        new Thread(this::listenForMessages).start();
//        portsMap = new ConcurrentHashMap<>();
//        portsMap.put("MTL", r.nextInt(60000-8000) + 8000);
//        portsMap.put("QUE", r.nextInt(60000-8000) + 8000);
//        portsMap.put("SHE", r.nextInt(60000-8000) + 8000);
    }

    public DERMSServer(String serverID, ConcurrentHashMap<String, Integer> m) throws InterruptedException {
        this.serverID = serverID;
        resources = new HashMap<>();
//        portsMap = new ConcurrentHashMap<>();
//        portsMap.put("MTL", r.nextInt(60000-8000) + 8000);
//        portsMap.put("QUE", r.nextInt(60000-8000) + 8000);
//        portsMap.put("SHE", r.nextInt(60000-8000) + 8000);
        portsMap = m;
        new Thread(this::listenForMessages).start();
        Thread.sleep(10);
    }

    private void listenForMessages() {
        try (DatagramSocket socket = new DatagramSocket(getServerPortsFromCentralRepo().get(serverID))) {
            this.serverPort.set(socket.getLocalPort());
            byte[] buffer = new byte[1024];
            System.out.println("Listening on port: " + socket.getLocalPort());

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.equals("DIE")) break;
                String response = handleUDPMessage(message);
                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String handleUDPMessage(String message) {
        String[] parts = message.split(" ");
        String command = parts[0];

        try {
            switch (command) {
                case "REQUEST_RESOURCE":
                    return requestResource(parts[1], parts[2], Integer.parseInt(parts[3]));
                case "FIND_RESOURCE":
                    return String.join(",", localFindResource(parts[1], parts[2]));
                case "LIST_RESOURCE_AVAILABILITY":
                    return String.join(",", localListResourceAvailability(parts[1]));
                case "DIE":
                    return "DIE";
                default:
                    return "UNKNOWN_COMMAND";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    private String sendMessage(String message, String host, int port) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);

        byte[] buffer = message.getBytes();
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
        System.out.println("Sending message: " + message + " to " + host + ":" + port);

        socket.send(request);

        byte[] responseBuffer = new byte[1024];
        DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);

        socket.receive(response);

        socket.close();

        return new String(response.getData(), 0, response.getLength());
    }

    @Override
    public synchronized String addResource(String resourceID,
                                           String resourceName,
                                           int duration) {
        String response;
        boolean success = false;
        String validation = validInput(resourceID, resourceName, duration);
        if (validation == null) {
            if (resources.containsKey(resourceID)) {
                Resource resource = resources.get(resourceID);
                if (duration > resource.getDuration()) {
                    resource.setDuration(duration);
                    response = "derms.Resource duration updated successfully.";
                    success = true;
                } else {
                    response = "derms.Resource already exists with equal or longer duration.";
                }
            } else {
                resources.put(resourceID, new Resource(resourceID, resourceName, duration));
                response = "Successfully added resource " + resourceID + " " + duration;
                success = true;
            }
        } else {
            response = validation;
        }
//        System.out.println(resources);
        logOperation("addResource", resourceID, resourceName, duration, success, response);
        return response;
    }

    @Override
    public synchronized String removeResource(String resourceID,
                                              int duration) {
        String response;
        boolean success = false;
        String validation = validInput(resourceID, resourceNames.get(0), duration);
        if (validation == null) {
            if (resources.containsKey(resourceID)) {
                Resource resource = resources.get(resourceID);
                if (duration < resource.getDuration()) {
                    resource.setDuration(resource.getDuration() - duration);
                    response = "derms.Resource duration decreased successfully.";
                } else {
                    resources.remove(resourceID);
                    response = "derms.Resource removed successfully.";
                }
                success = true;
            } else {
                response = "derms.Resource does not exist.";
            }
        } else {
            response = validation;
        }
        logOperation("removeResource", resourceID, duration, success, response);
        return response;
    }

    @Override
    public List<String> listResourceAvailability(String resourceName) {
        String requestID = UUID.randomUUID().toString();
        return listResourceAvailability(requestID, resourceName);
    }

    public List<String> localListResourceAvailability(String resourceName) {
        List<String> availableResources = new ArrayList<>();
        for (Resource resource : resources.values()) {
            if (resource.getResourceName().equals(resourceName)) {
                availableResources.add(resource.toString());
            }
        }
        return availableResources;
    }

    public List<String> listResourceAvailability(String requestID, String resourceName) {
        if (!requests.contains(requestID)) {
            requests.add(requestID);
        } else {
            return Arrays.asList();
        }

        // Assuming a method for fetching the server names and ports via web services
        Map<String, Integer> serverNames = getServerPortsFromCentralRepo();

        serverNames.remove(serverID);
        List<Callable<List<String>>> callables = new ArrayList<>();
        for (Map.Entry<String, Integer> server : serverNames.entrySet()) {
            callables.add(() -> Arrays.asList(sendMessage(String.format("LIST_RESOURCE_AVAILABILITY %s", resourceName), "localhost", server.getValue()).split(",")));
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<List<String>>> results;
        try {
            results = executor.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> availableResources = new ArrayList<>();
        for (Resource resource : resources.values()) {
            if (resource.getResourceName().equals(resourceName)) {
                availableResources.add(resource.toString());
            }
        }
        for (Future<List<String>> f : results) {
            try {
                availableResources.addAll(f.get().stream().filter((s) -> !s.isEmpty()).collect(Collectors.toList()));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        logOperation("listResourceAvailability", serverID, true, availableResources.toString());
        return availableResources;
    }

    private Map<String, Integer> getServerPortsFromCentralRepo() {
        // Mocking response as web services since no derms.CentralRepoInterface.
        return new HashMap<>(portsMap);
    }

    @Override
    public synchronized String requestResource(String coordinatorID,
                                               String resourceID,
                                               int duration) {
        String response;
        boolean success = false;
        List<String> others = new ArrayList<>(getServerPortsFromCentralRepo().keySet());
        others.remove(serverID);

        String validation = validInput(resourceID, resourceNames.get(0), duration);
        if (validation == null) {
//            System.out.println(resources);
            if (resources.containsKey(resourceID)) {
                Resource resource = resources.get(resourceID);
                if (duration <= resource.getDuration() && !resource.borrowed) {
                    resource.setDuration(resource.getDuration() - duration);
                    resource.borrowed = true;
                    resource.borrower = coordinatorID;
                    response = "derms.Resource requested successfully.";
                    success = true;
                } else {
                    resource.requesters.add(0, new Requester(coordinatorID, duration));
                    response = "Insufficient resource duration.";
                }
            } else if (others.contains(resourceID.substring(0, 3))) {
                try {
                    return sendMessage(String.format("REQUEST_RESOURCE %s %s %d", coordinatorID, resourceID, duration), "localhost", getServerPortsFromCentralRepo().get(resourceID.substring(0, 3)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                response = "derms.Resource does not exist.";
            }
        } else {
            response = validation;
        }
        logOperation("requestResource", coordinatorID, resourceID, duration, success, response);
        return response;
    }

    @Override
    public List<String> findResource(String coordinatorID,
                                     String resourceName) {
        String requestID = UUID.randomUUID().toString();
        return findResource(requestID, coordinatorID, resourceName);
    }

    public List<String> localFindResource(String coordinatorID,
                                         String resourceName) {
        List<String> foundResources = new ArrayList<>();
        for (Resource resource : resources.values()) {
            if (resource.getResourceName().equals(resourceName)) {
                foundResources.add(resource.toString());
            }
        }
        return foundResources;
    }

    public List<String> findResource(String requestID, String coordinatorID, String resourceName) {
        if (!requests.contains(requestID)) {
            requests.add(requestID);
        } else {
            return Arrays.asList();
        }

        if (!resourceNames.contains(resourceName)) {
            logOperation("findResource", coordinatorID, resourceName, false, "No resources found.");
             return Arrays.asList();
        }

        Map<String, Integer> serverNames = getServerPortsFromCentralRepo();

        serverNames.remove(serverID);
        List<Callable<List<String>>> callables = new ArrayList<>();
        for (Map.Entry<String, Integer> server : serverNames.entrySet()) {
            callables.add(() -> Arrays.asList(sendMessage(String.format("FIND_RESOURCE %s %s", coordinatorID, resourceName), "localhost", server.getValue()).split(",")));
        }
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<List<String>>> results;
        try {
            results = executor.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> foundResources = new ArrayList<>();
        for (Resource resource : resources.values()) {
            if (resource.getResourceName().equals(resourceName)) {
                foundResources.add(resource.toString());
            }
        }
        for (Future<List<String>> f : results) {
            try {
                foundResources.addAll(f.get().stream().filter((s) -> !s.isEmpty()).collect(Collectors.toList()));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        logOperation("findResource", coordinatorID, resourceName, true, foundResources.toString());
        return foundResources;
    }

    @Override
    public synchronized String returnResource(String coordinatorID,
                                              String resourceID) {
        String response;
        boolean success = false;
        String validation = validInput(resourceID, resourceNames.get(0), 1);
        if (validation == null) {
            if (resources.containsKey(resourceID)) {
                Resource resource = resources.get(resourceID);
                if (Objects.equals(resource.borrower, coordinatorID)) {
                    resource.borrower = null;
                    resource.borrowed = false;
                    response = "derms.Resource returned successfully.";
                    success = true;
                    if (!resource.requesters.isEmpty()) {
                        Requester r = resource.requesters.remove(resource.requesters.size() - 1);
                        requestResource(r.getCoordinatorID(), resourceID, r.getDuration());
                    }
                } else {
                    response = "You are not the requester.";
                }
            } else {
                response = "derms.Resource does not exist.";
                success = false;
            }
        } else {
            response = validation;
        }
        logOperation("returnResource", coordinatorID, resourceID, success, response);
        return response;
    }

    @Override
    public String swapResource(String coordinatorID,
                               String oldResourceID,
                               String oldResourceType,
                               String newResourceID,
                               String newResourceType) {
        String response;
        boolean success = false;
        Map<String, Integer> others = getServerPortsFromCentralRepo();
        others.remove(serverID);

        String validation1 = validInput(oldResourceID, oldResourceType, 1);
        if (validation1 != null) {
            logOperation("swapResource", coordinatorID, oldResourceID, -1, false, validation1);
            return validation1;
        }
        String validation2 = validInput(newResourceID, newResourceType, 1);
        if (validation2 != null) {
            logOperation("swapResource", coordinatorID, newResourceID, -1, false, validation2);
            return validation2;
        }

        if (resources.containsKey(oldResourceID)) {
            Resource resource = resources.get(oldResourceID);
            if (resource.borrowed && Objects.equals(resource.borrower, coordinatorID)) {
                if (others.containsKey(newResourceID.substring(0, 3))) {
                    try {
                        String s = sendMessage(String.format("REQUEST_RESOURCE %s %s %d", coordinatorID, newResourceID, resource.getDuration()), "localhost", others.get(newResourceID.substring(0, 3)));
//                        System.out.println(s);
                        if (s.equals("derms.Resource requested successfully.")) {
                            returnResource(coordinatorID, oldResourceID);
                            response = "derms.Resource swapped successfully.";
                            success = true;
                        } else if(s.equals("Insufficient resource duration.")){
                            response = "Could not request newResource.";
                        }
                        else if(s.equals("derms.Resource does not exist.")) { System.out.println("NEW");
                            response = "derms.Resource does not exist.";
                        }
                        else{
                            response = s;}

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else
                {
                    response = "Incorrect server name";
                }
            } else {
                response = "You are not the requester.";
            }
        } else { System.out.println("old");

            response = "derms.Resource does not exist.";
        }
        logOperation("swapResource", coordinatorID, newResourceID, success, response);
        return response;
    }

    private void logOperation(String operation, String resourceID, String resourceName, int duration, boolean success, String response) {
        try (FileWriter writer = new FileWriter(serverID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + resourceID + " - " + resourceName + " - " + duration + " - " + (success ? "SUCCESS" : "FAILURE") + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOperation(String operation, String resourceID, int duration, boolean success, String response) {
        try (FileWriter writer = new FileWriter(serverID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + resourceID + " - " + duration + " - " + (success ? "SUCCESS" : "FAILURE") + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOperation(String operation, String resourceName, boolean success, String response) {
        try (FileWriter writer = new FileWriter(serverID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + resourceName + " - " + (success ? "SUCCESS" : "FAILURE") + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOperation(String operation, String coordinatorID, String resourceName, boolean success, String response) {
        try (FileWriter writer = new FileWriter(serverID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + coordinatorID + " - " + resourceName + " - " + (success ? "SUCCESS" : "FAILURE") + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String validInput(String resourceID, String resourceName, int duration) {
        if (resourceID.length() != 7 || !cities.contains(resourceID.substring(0, 3))) {
            return "Invalid resource ID.";
        }
        try {
            Integer.valueOf(resourceID.substring(3));
        } catch (Exception e) {
            return "Invalid resource ID.";
        }
        if (!resourceNames.contains(resourceName)) {
            return "Invalid resource type.";
        }
        if (duration < 0) {
            return "Invalid duration.";
        }
        return null;
    }

    class Resource {
        private String resourceID;
        private String resourceName;
        private int duration;
        private boolean borrowed = false;
        private String borrower;
        private List<Requester> requesters = new ArrayList<>();

        public Resource(String resourceID, String resourceName, int duration) {
            this.resourceID = resourceID;
            this.resourceName = resourceName;
            this.duration = duration;
        }

        public String getResourceName() {
            return resourceName;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }




        @Override
        public String toString() {
            return String.format("derms.Resource{ID=%s, Name=%s, Duration=%d, Borrowed=%b, Borrower=%s}",
                    resourceID, resourceName, duration, borrowed, borrower);
        }
    }
}