//Server.java
package derms.Replica4pkg;

import javax.jws.WebService;
import javax.jws.WebMethod;
import java.io.*;
import java.net.*;
import java.util.*;

@WebService(endpointInterface = "derms.Replica4pkg.DERMSInterface")
public class Server implements DERMSInterface {
    private String serverName;
    private int udpPort;
    private Map<String, Map<String, Resource>> resourceMap; //resourceName -> (resourceID -> Resource)
    private Map<String, Set<String>> coordinatorResources; //coordinatorID -> Set<resourceID>
    private Map<String, Map<String, Integer>> coordinatorResourceDurations; //coordinatorID -> (resourceID -> allocatedDuration)
    private Map<String, Integer> serverUdpPorts; //serverName -> UDP port


    public Server() {
        this.resourceMap = new HashMap<>();
        this.coordinatorResources = new HashMap<>();
        this.coordinatorResourceDurations = new HashMap<>();
        startUDPListener();
    }

    @WebMethod(exclude = true)
    public void initServer(String serverName, int udpPort, Map<String, Integer> serverUdpPorts) {
        this.serverName = serverName;
        this.udpPort = udpPort;
        this.serverUdpPorts = serverUdpPorts;
    }

    //Start UDP Listener
    private void startUDPListener() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(udpPort)) {
                byte[] buffer = new byte[4096];
                System.out.println(serverName + " UDP Server started on port " + udpPort);

                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    socket.receive(request);
                    new Thread(() -> handleUDPRequest(request)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Handle UDP Request
    private void handleUDPRequest(DatagramPacket requestPacket) {
        try {
            String requestData = new String(requestPacket.getData(), 0, requestPacket.getLength());
            String[] parts = requestData.split(":");
            String methodName = parts[0];
            String[] params = Arrays.copyOfRange(parts, 1, parts.length);

            String responseData = "";

            switch (methodName) {
                case "listResourceAvailability":
                    responseData = handleListResourceAvailability(params[0]);
                    break;
                case "requestResource":
                    responseData = handleLocalResourceRequest(params[0], params[1], Integer.parseInt(params[2]));
                    break;
                case "findResource":
                    responseData = handleFindResource(params[0], params[1]);
                    break;
                case "returnResource":
                    responseData = handleReturnResource(params[0], params[1]);
                    break;
                case "swapRequestResource":
                    responseData = handleSwapRequestResource(params[0], params[1], Integer.parseInt(params[2]));
                    break;    
                case "swapReturnResource":
                    responseData = handleSwapReturnResource(params[0], params[1]);
                    break;
                case "checkCoordinatorResource":
                    responseData = checkCoordinatorHasResource(params[0], params[1]);
                    break;
                case "getCoordinatorResourceDuration":
                    int duration = getCoordinatorResourceDuration(params[0], params[1]);
                    responseData = String.valueOf(duration);
                    break;
                default:
                    responseData = "Invalid method";
            }

            //Send response
            byte[] responseBytes = responseData.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, requestPacket.getAddress(), requestPacket.getPort());
            DatagramSocket socket = new DatagramSocket();
            socket.send(responsePacket);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Operation Handlers
    private synchronized String handleListResourceAvailability(String resourceName) {
        StringBuilder result = new StringBuilder();
        resourceName = resourceName.toUpperCase();
        Map<String, Resource> resources = resourceMap.get(resourceName);

        if (resources != null) {
            for (Resource resource : resources.values()) {
            	if (resource.getDuration() > 0) {
                    result.append(resource.getResourceName()).append(" - ").append(serverName).append(" ").append(resource.getDuration()).append("\n");
            	}
            }
        }
        return result.toString();
    }

    private synchronized String handleLocalResourceRequest(String coordinatorID, String resourceID, int duration) {
        for (Map<String, Resource> resources : resourceMap.values()) {
            Resource resource = resources.get(resourceID);
            if (resource != null) {
                if (resource.getDuration() >= duration) {
                    resource.subDuration(duration);
                    coordinatorResources.computeIfAbsent(coordinatorID, k -> new HashSet<>()).add(resourceID);
                    coordinatorResourceDurations.computeIfAbsent(coordinatorID, k -> new HashMap<>()).put(resourceID, duration);

                    log("Resource " + resourceID + " allocated to " + coordinatorID + " for duration " + duration);
                    return "Resource " + resourceID + " allocated successfully.";
                } else {
                    return "Insufficient duration for resource " + resourceID;
                }
            }
        }
        return "Resource " + resourceID + " not found.";
    }

    private synchronized String handleFindResource(String coordinatorID, String resourceName) {
        StringBuilder result = new StringBuilder();
        resourceName = resourceName.toUpperCase();
        Set<String> resources = coordinatorResources.get(coordinatorID);
        if (resources != null) {
            Map<String, Integer> resourceDurations = coordinatorResourceDurations.get(coordinatorID);
            if (resourceDurations != null) {
                for (String resourceID : resources) {
                    Resource resource = getResourceByID(resourceID);
                    if (resource != null && resource.getResourceName().equals(resourceName)) {
                        Integer allocatedDuration = resourceDurations.get(resourceID);
                        if (allocatedDuration != null) {
                            result.append(resource.getResourceName()).append(" - ").append(resourceID).append(" ").append(allocatedDuration).append("\n");
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    private synchronized String handleReturnResource(String coordinatorID, String resourceID) {
        Set<String> resources = coordinatorResources.get(coordinatorID);
        if (resources != null && resources.contains(resourceID)) {
            resources.remove(resourceID);
            if (resources.isEmpty()) {
                coordinatorResources.remove(coordinatorID);
            }
            Resource resource = getResourceByID(resourceID);
            if (resource != null) {
                Map<String, Integer> resourceDurations = coordinatorResourceDurations.get(coordinatorID);
                if (resourceDurations != null) {
                    Integer allocatedDuration = resourceDurations.remove(resourceID);
                    if (allocatedDuration != null) {
                        resource.addDuration(allocatedDuration);
                    }
                    if (resourceDurations.isEmpty()) {
                        coordinatorResourceDurations.remove(coordinatorID);
                    }
                }
                log("Coordinator " + coordinatorID + " returned resource " + resourceID);
                return "Resource " + resourceID + " returned successfully.";
            } else {
                return "Resource " + resourceID + " not found.";
            }
        } else {
            return "You do not occupy resource " + resourceID + ".";
        }
    }

    //Helper Methods
    private Resource getResourceByID(String resourceID) {
        for (Map<String, Resource> resources : resourceMap.values()) {
            if (resources.containsKey(resourceID)) {
                return resources.get(resourceID);
            }
        }
        return null;
    }
    
    private synchronized String handleSwapRequestResource(String coordinatorID, String resourceID, int duration) {
        //Attempt to allocate the resource to the coordinator
        for (Map<String, Resource> resources : resourceMap.values()) {
            Resource resource = resources.get(resourceID);
            if (resource != null) {
                if (resource.getDuration() >= duration) {
                    resource.subDuration(duration);
                    coordinatorResources.computeIfAbsent(coordinatorID, k -> new HashSet<>()).add(resourceID);
                    coordinatorResourceDurations.computeIfAbsent(coordinatorID, k -> new HashMap<>()).put(resourceID, duration);
                    log("Resource " + resourceID + " temporarily allocated to " + coordinatorID + " for swapping, duration " + duration);
                    return "Success";
                } else {
                    return "Insufficient duration for resource " + resourceID;
                }
            }
        }
        return "Resource " + resourceID + " not found.";
    }
    
    private synchronized String handleSwapReturnResource(String coordinatorID, String resourceID) {
        Set<String> resources = coordinatorResources.get(coordinatorID);
        if (resources != null && resources.contains(resourceID)) {
            resources.remove(resourceID);
            if (resources.isEmpty()) {
                coordinatorResources.remove(coordinatorID);
            }
            Resource resource = getResourceByID(resourceID);
            if (resource != null) {
                Map<String, Integer> resourceDurations = coordinatorResourceDurations.get(coordinatorID);
                if (resourceDurations != null) {
                    Integer allocatedDuration = resourceDurations.remove(resourceID);
                    if (allocatedDuration != null) {
                        resource.addDuration(allocatedDuration);
                    }
                    if (resourceDurations.isEmpty()) {
                        coordinatorResourceDurations.remove(coordinatorID);
                    }
                }
                log("Coordinator " + coordinatorID + " resource " + resourceID + " allocation undone during swap");
                return "Success";
            } else {
                return "Resource " + resourceID + " not found.";
            }
        } else {
            return "Coordinator did not acquire resource " + resourceID;
        }
    }

    private String sendUDPRequest(String serverName, String methodName, String... params) {
        String response = "";
        try {
            InetAddress host = InetAddress.getByName("localhost");
            int port = serverUdpPorts.get(serverName);
            String requestData = methodName + ":" + String.join(":", params);

            byte[] requestBytes = requestData.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, host, port);

            DatagramSocket socket = new DatagramSocket();
            socket.send(requestPacket);

            byte[] buffer = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    //Implement ResponderInterface Methods
    @Override
    public synchronized String addResource(String resourceID, String resourceName, int duration) {
        resourceName = resourceName.toUpperCase();
        Map<String, Resource> resources = resourceMap.computeIfAbsent(resourceName, k -> new HashMap<>());
        Resource resource = resources.get(resourceID);

        if (resource != null) {
            if (duration > resource.getDuration()) {
                resource.setDuration(duration);
                log("Updated duration of resource " + resourceID);
                return "Resource " + resourceID + " duration updated.";
            } else {
                log("Resource " + resourceID + " already exists with greater or equal duration.");
                return "Resource " + resourceID + " already exists with greater or equal duration.";
            }
        } else {
            resource = new Resource(resourceName, duration);
            resources.put(resourceID, resource);
            log("Added new resource " + resourceID);
            return "Resource " + resourceID + " added successfully.";
        }
    }

    @Override
    public synchronized String removeResource(String resourceID, int duration) {
        for (Map<String, Resource> resources : resourceMap.values()) {
            Resource resource = resources.get(resourceID);
            if (resource != null) {
                if (duration >= resource.getDuration()) {
                    resources.remove(resourceID);
                    log("Resource " + resourceID + " completely removed.");
                    return "Resource " + resourceID + " completely removed.";
                } else {
                    resource.subDuration(duration);
                    log("Decreased duration of resource " + resourceID);
                    return "Resource " + resourceID + " duration decreased by " + duration + ".";
                }
            }
        }
        log("Resource " + resourceID + " not found.");
        return "Resource " + resourceID + " not found.";
    }

    @Override
    public String listResourceAvailability(String resourceName) {
        resourceName = resourceName.toUpperCase();
        StringBuilder result = new StringBuilder();

        synchronized (this) {
            Map<String, Resource> resources = resourceMap.get(resourceName);
            if (resources != null) {
                for (Resource resource : resources.values()) {
                	if (resource.getDuration() > 0) {
	                    result.append(resource.getResourceName()).append(" - ").append(serverName).append(" ").append(resource.getDuration()).append("\n");
	                }
                }
            }
        }

        for (String otherServer : serverUdpPorts.keySet()) {
            if (!otherServer.equals(serverName)) {
                String response = sendUDPRequest(otherServer, "listResourceAvailability", resourceName);
                result.append(response);
            }
        }

        return result.toString();
    }

    @Override
    public String requestResource(String coordinatorID, String resourceID, int duration) {
        String resourceServer = resourceID.substring(0, 3).toUpperCase();
        String response;

        if (resourceServer.equals(serverName)) {
            response = handleLocalResourceRequest(coordinatorID, resourceID, duration);
        } else {
            response = sendUDPRequest(resourceServer, "requestResource", coordinatorID, resourceID, String.valueOf(duration));
        }

        log("Coordinator " + coordinatorID + " requested resource " + resourceID + ": " + response);
        return response;
    }

    @Override
    public String findResource(String coordinatorID, String resourceName) {
        resourceName = resourceName.toUpperCase();
        StringBuilder result = new StringBuilder();

        for (String otherServer : serverUdpPorts.keySet()) {
            String response;
            if (otherServer.equals(serverName)) {
                response = handleFindResource(coordinatorID, resourceName);
            } else {
                response = sendUDPRequest(otherServer, "findResource", coordinatorID, resourceName);
            }
            result.append(response);
        }

        return result.toString();
    }

    @Override
    public String returnResource(String coordinatorID, String resourceID) {
        String resourceServer = resourceID.substring(0, 3).toUpperCase();
        String response;

        if (resourceServer.equals(serverName)) {
            response = handleReturnResource(coordinatorID, resourceID);
        } else {
            response = sendUDPRequest(resourceServer, "returnResource", coordinatorID, resourceID);
        }

        log("Coordinator " + coordinatorID + " returned resource " + resourceID + ": " + response);
        return response;
    }
    
    private synchronized int getCoordinatorResourceDuration(String coordinatorID, String resourceID) {
        Map<String, Integer> resourceDurations = coordinatorResourceDurations.get(coordinatorID);
        if (resourceDurations != null && resourceDurations.containsKey(resourceID)) {
            return resourceDurations.get(resourceID);
        }
        return 0;
    }
    
    private synchronized String checkCoordinatorHasResource(String coordinatorID, String resourceID) {
        Set<String> resources = coordinatorResources.get(coordinatorID);
        if (resources != null && resources.contains(resourceID)) {
            return "true";
        }
        return "false";
    }
    
    @Override
    public String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        String oldResourceServer = oldResourceID.substring(0, 3).toUpperCase();
        String newResourceServer = newResourceID.substring(0, 3).toUpperCase();

        //Check if the coordinator has the old resource
        String checkOldResourceResponse;
        if (oldResourceServer.equals(serverName)) {
            checkOldResourceResponse = checkCoordinatorHasResource(coordinatorID, oldResourceID);
        } else {
            checkOldResourceResponse = sendUDPRequest(oldResourceServer, "checkCoordinatorResource", coordinatorID, oldResourceID);
        }

        if (!checkOldResourceResponse.equals("true")) {
            return "Coordinator has not acquired the old resource.";
        }

        //Get the duration allocated to the old resource
        int duration;
        if (oldResourceServer.equals(serverName)) {
            duration = getCoordinatorResourceDuration(coordinatorID, oldResourceID);
        } else {
            String durationStr = sendUDPRequest(oldResourceServer, "getCoordinatorResourceDuration", coordinatorID, oldResourceID);
            duration = Integer.parseInt(durationStr);
        }

        //Attempt to acquire the new resource
        String requestResponse;
        if (newResourceServer.equals(serverName)) {
            requestResponse = handleLocalResourceRequest(coordinatorID, newResourceID, duration);
        } else {
            requestResponse = sendUDPRequest(newResourceServer, "requestResource", coordinatorID, newResourceID, String.valueOf(duration));
        }

        if (!requestResponse.contains("allocated successfully")) {
            return "Failed to acquire new resource: " + requestResponse;
        }

        //Return the old resource
        String returnResponse;
        if (oldResourceServer.equals(serverName)) {
            returnResponse = handleReturnResource(coordinatorID, oldResourceID);
        } else {
            returnResponse = sendUDPRequest(oldResourceServer, "returnResource", coordinatorID, oldResourceID);
        }

        if (!returnResponse.contains("returned successfully")) {
            //Undo the allocation of the new resource
            if (newResourceServer.equals(serverName)) {
                handleReturnResource(coordinatorID, newResourceID);
            } else {
                sendUDPRequest(newResourceServer, "returnResource", coordinatorID, newResourceID);
            }
            return "Failed to return old resource: " + returnResponse;
        }

        log("Coordinator " + coordinatorID + " swapped resource " + oldResourceID + " with " + newResourceID);

        return "Resource swap successful.";
    }

    //Logging method
    public synchronized void log(String message) {
        String logMessage = new Date() + " - " + message;
        System.out.println(logMessage);

        try (FileWriter fw = new FileWriter(serverName + "_server_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}