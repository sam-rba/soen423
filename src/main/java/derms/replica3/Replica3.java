package derms.replica3;

import derms.Replica;
import derms.ReplicaManager;
import derms.Request;
import derms.Response;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import derms.replica3.Logger;

public class Replica3 implements Replica{
    static final InetSocketAddress announceGroup = new InetSocketAddress("225.5.5.5", 5555);

    private String serverID;
    private HashMap<String, HashMap<String, Resource>> resources;
    private HashMap<String, List<Resource>> coordinatorResources ;
    private HashMap<String, List<String>> resourceWaitingQueues;
    private List<String> serverList = Arrays.asList(Constants.MONTREAL, Constants.QUEBEC, Constants.SHERBROOKE);
    private Logger activityLogger;
//    final InetAddress localAddr;
//    private final Logger log;

    private boolean alive;
    private final ReplicaManager replicaManager;

    public Replica3(City city, ReplicaManager replicaManager) throws IOException {
        this.serverID = city.getCode();
        this.resources = new HashMap<>();
        coordinatorResources = new HashMap<>();
        resourceWaitingQueues = new HashMap<>();
//        this.log = DermsLogger.getLogger(getClass());
        this.replicaManager = replicaManager;
        this.activityLogger = new Logger( serverID + "Server.log");

//        log.info("Running");
//        log.config("Local address is "+localAddr.toString());

        this.alive = true;
    }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public void startProcess() {
        // TODO
//        log.info(getClass().getSimpleName() + " started.");
        System.out.println("process started");
    }

    @Override
    public void processRequest(Request request) {
//        log.info(request.toString());
        System.out.println("process request and good");
        String status = "";
        try {
            switch (request.getFunction()) {
                case "addResource":
                    status = addResource(request.getResourceID(), request.getResourceType(), request.getDuration());
                    break;
                case "removeResource":
                    status = removeResource(request.getResourceID(), request.getDuration());
                    break;
                case "listResourceAvailability":
                    status = listResourceAvailability(request.getResourceType());
                    break;
                case "requestResource":
                    status = requestResource(request.getClientID(), request.getResourceID(), request.getDuration());
                    break;
                case "findResource":
                    status = findResource(request.getClientID(), request.getResourceType());
                    break;
                case "returnResource":
                    status = returnResource(request.getClientID(), request.getResourceID());
                    break;
                case "swapResource":
                    status = swapResource(request.getClientID(),request.getOldResourceID(), request.getOldResourceType(), request.getResourceID(), request.getResourceType());
                    break;
                default:
                    status = "Failure: unknown function '" + request.getFunction() + "'";
            }
        } catch (Exception e) {
//            log.warning(e.getMessage());
            status = "Failure: " + request.getFunction() + ": " + e.getMessage();
        }

        Response response = new Response(request.getSequenceNumber(), status);
//        log.info("Processed request " + request + "; response: " + response);
        replicaManager.sendResponseToFE(response);
    }


    @Override
    public void restart() {
        // TODO
        shutdown();
        startProcess();
    }

    @Override
    public int getId() { return 3; }

    private void shutdown() {
        // TODO
    }

    public synchronized String addResource(String resourceID, String resourceName, int duration) {
        // Check if the resource name already exists
        String message = "";
//        boolean isIdValid = ValidationService.isResourceIDValid(resourceID);
//        boolean isNameValid = ValidationService.isResourceNameValid(resourceName);
//
//        if(!(isIdValid && isNameValid)) {
//            message = "Input is not valid for ID: " + resourceID + " " + resourceName + " " + duration;
//            activityLogger.log(" ADD RESOURCE (" + resourceID + ", " + resourceName + ", " + duration + ") " , " FAILED ", message);
//            return message;
//        }

        HashMap<String, Resource> resourceMap = resources.get(resourceName);
        if (resourceMap == null) {
            resourceMap = new HashMap<>();  // Create a new inner HashMap if it doesn't exist
            resources.put(resourceName, resourceMap);
        }

        Resource resource = resourceMap.get(resourceID);
        if (resource != null) {
            // If the resource already exists, update its duration
            if (duration > resource.getDuration()) {
                resource.setDuration(duration);
            }
            message = "Resource already exist! So duration is updated: " + resourceID + " : " + resourceName + "  : " + duration;
        } else {
            // Add the new resource
            resource = new Resource(resourceID, resourceName, duration);
            resourceMap.put(resourceID, resource);
            message = "New Resource of " + resourceName  +" is added: " + resourceID + " : " + resourceName + "  : " + duration;
        }

//        if(resourceWaitingQueues.containsKey(resourceID)) {
//            List<String> coordinators = resourceWaitingQueues.get(resourceID);
//            for(int i=0; i<coordinators.size(); i++) {
//                if(coordinators.get(i).split("-").length > 1) {
//                    String crID = coordinators.get(i).split("-")[0];
//                    int dur = Integer.parseInt(coordinators.get(i).split("-")[1]);
//                    Resource res = resourceMap.get(resourceID);
//
//                    if(res.getDuration() >= dur) {
////                        activityLogger.log(" ADD RESOURCE - Resource allocation to waiting coordinator", " COMPLETED ", "Resource " +
////                                resourceID + " is allocated to waiting coordinator " + crID);
//                        res.setDuration(res.getDuration() - dur);
//                        resourceMap.put(resourceID, res);
//                        coordinators.remove(i);
//                        break;
//                    }
//                }
//
//            }
//        }
//        activityLogger.log(" ADD RESOURCE (" + resourceID + ", " + resourceName + ", " + duration + ") " , " COMPLETED ", message);

        return message;
    }

    public synchronized String removeResource(String resourceID, int duration) {
        String message = "";

//        boolean isIdValid = ValidationService.isResourceIDValid(resourceID);
//
//        if(!isIdValid ) {
//            message = "Input is not valid for ID: " + resourceID  + " " + duration;
//            activityLogger.log(" ADD RESOURCE (" + resourceID  + ", " + duration + ") " , " FAILED ", message);
//            return message;
//        }

        for (HashMap<String, Resource> resourceMap : resources.values()) {
            Resource resource = resourceMap.get(resourceID);

            if (resource != null) {
                if (duration >= resource.getDuration()) {
                    resourceMap.remove(resourceID);  // Remove if duration is fully used
                    message = "Resource of ID: " + resourceID + " with duration " + resource.getDuration() + " is successfully removed.";
                    activityLogger.log(" REMOVE RESOURCE (" + resourceID + ", " + duration + ") " , " COMPLETED ", message);
                    return message;
                } else {
                    resource.setDuration(resource.getDuration() - duration);  // Reduce the duration
                    message = "Duration of the Resource of ID: " + resourceID + " with duration " + duration + " is successfully reduced.";
                    activityLogger.log(" REMOVE RESOURCE (" + resourceID + ", " + duration + ") " , " COMPLETED ", message);
                    return message;
                }
            }
        }
        message = "Resource with ID " + resourceID + " is not found!";
        activityLogger.log(" REMOVE RESOURCE (" + resourceID + ", " + duration + ") " , " FAILED ", message);

        return "Resource not found";
    }

    public synchronized String listResourceAvailability(String resourceName) {
        String message = "";

//        boolean isNameValid = ValidationService.isResourceNameValid(resourceName);
//
//        if(!isNameValid ) {
//            message = "Input is not valid for name: " + resourceName;
//            activityLogger.log(" LIST RESOURCE AVAILABILITY (" + resourceName  + ") " , " FAILED ", message);
//            return message;
//        }

        final StringBuilder result =  new StringBuilder();

        result.append(resourceName.toUpperCase());
        CountDownLatch latch = new CountDownLatch(serverList.size() - 1);

        for(String server:  serverList) {
            System.out.println(serverID + " " + server);

            if(serverID.equalsIgnoreCase(server)) {
                result.append(getAvailableResources(resourceName));
                System.out.println("listResourceAvailability same server:" + result);
            }else {
                System.out.println("listResourceAvailability request region:" + result);
                new Thread(() -> {
                    System.out.println("listResourceAvailability Thread:" + result);
                    final String count = sendResourceAvailabilityRequest(server, resourceName);
                    System.out.println(count);
                    if(count != null)
                        synchronized(result) {
                            result.append(count);
                        }
                    latch.countDown();
                }).start();
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activityLogger.log(" LIST RESOURCE AVAILABILITY (" + resourceName  + ") " , " COMPLETED ", result.toString());
        System.out.println("printing: " + result.toString());
        return result.toString().trim();
    }

    public String getAvailableResources(String resourceName) {
        String result = "";

        HashMap<String, Resource> resourceMap = resources.get(resourceName);
        if (resourceMap == null) {
            return result;
        }

        for (Resource resource : resourceMap.values()) {
            result += " " + resource.getResourceID() + "-" + resource.getDuration();
        }

        return result;
    }

    private String sendResourceAvailabilityRequest(final String location, String resourceName) {
        System.out.println("sendResourceAvailabilityRequest:");
        String recordCount = "";
        try {
            final InetAddress inetAddress = InetAddress.getByName("localhost");
            activityLogger.log(location, " UPD MESSAGE SENT ", "UPD Message sent for Resource Availability");

            final DatagramSocket socket = new DatagramSocket();
            byte[] data = (serverID + "-GET_AVAILABLE_RESOURCES" + "-" + resourceName ).toString().getBytes();

            final DatagramPacket packet = new DatagramPacket(data, data.length, inetAddress, PortConstants.getUdpPort(location)) ;
            socket.send(packet);
            data = new byte[1000];
            DatagramPacket responsePacket = new DatagramPacket(data, data.length);
            socket.receive(responsePacket);
            recordCount = " " + new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
            socket.close();
        } catch (Exception e) {
            activityLogger.log("", "EXCEPTION", e.getMessage());
        }
        return recordCount;
    }

    public synchronized String requestResource(String coordinatorID, String resourceID, int duration) {
        // Iterate through the outer HashMap to find the resource by its ID
        String message = "";
        final StringBuilder result =  new StringBuilder();

//        boolean isUserIdValid = ValidationService.isUserIDValid(coordinatorID);
//        boolean isIdValid = ValidationService.isResourceIDValid(resourceID);
//
//        if(!(isIdValid && isUserIdValid)) {
//            message = "Input is not valid for ID: " + resourceID + " " + resourceID + " " + duration;
//            activityLogger.log(" ADD RESOURCE (" + resourceID + ", " + resourceID + ", " + duration + ") " , " FAILED ", message);
//            return message;
//        }

        CountDownLatch latch = new CountDownLatch(1);

        for (HashMap<String, Resource> resourceMap : resources.values()) {
            Resource resource = resourceMap.get(resourceID);
            if (resource != null && resource.getDuration() >= duration) {
                // If resource is found and has sufficient duration, reduce it

                resource.setDuration(resource.getDuration() - duration);
                if(coordinatorResources.containsKey(coordinatorID)) {
                    Resource rs = new Resource(resourceID, resource.getResourceName(), duration);
                    coordinatorResources.get(coordinatorID).add(rs);
                }else {
                    Resource rs = new Resource(resourceID, resource.getResourceName(), duration);
                    List<Resource> lst = new ArrayList<Resource>();
                    lst.add(rs);
                    System.out.println(lst);
                    coordinatorResources.put(coordinatorID, lst);
                    System.out.println(coordinatorResources.keySet());
                }
                message = "Coordinator of ID " + coordinatorID + " borrowed resource of ID " + resourceID + " " + "from same server";

                activityLogger.log(" REQUEST RESOURCE (" + coordinatorID + ", " + resourceID + ", " + duration + ") " , " COMPLETED ", message);
                return message;
            } else if (resource != null) {
                message = "Insufficient resource duration for resource of ID " + resourceID + " " + " in the same server" ;
                activityLogger.log(" REQUEST RESOURCE (" + coordinatorID + ", " + resourceID + ", " + duration + ") " , " FAILED ", message);
                if(resourceWaitingQueues.containsKey(resourceID)) {
                    resourceWaitingQueues.get(resourceID).add(coordinatorID + "-" + duration);
                }else {
                    List<String> lst = new ArrayList<String>();
                    lst.add(coordinatorID + "-" + duration);
                    resourceWaitingQueues.put(resourceID, lst);
                }
                return message;
            }
        }

        for(String server:  serverList) {
            if(!server.equalsIgnoreCase(serverID) && resourceID.substring(0,3).equalsIgnoreCase(server)) {
                new Thread(() -> {
                    final String count = sendResourceRequest(server, resourceID, duration);
                    if(count != null) {
                        if(count.trim().length() > 0) {
                            if(coordinatorResources.containsKey(coordinatorID)) {
                                Resource rs = new Resource(resourceID, "", duration);
                                coordinatorResources.get(coordinatorID).add(rs);
                            }else {
                                List<Resource> lst = new ArrayList<Resource>();
                                Resource rs = new Resource(resourceID, "", duration);
                                lst.add(rs);
                                coordinatorResources.put(coordinatorID, lst);
                            }
                        }else {
                            if(resourceWaitingQueues.containsKey(resourceID)) {
                                resourceWaitingQueues.get(resourceID).add(coordinatorID);
                            }else {
                                List<String> lst = new ArrayList<String>();
                                lst.add(coordinatorID);
                                resourceWaitingQueues.put(resourceID, lst);
                            }

                        }
                        synchronized(result) {
                            result.append(count);
                        }
                    }
                    latch.countDown();
                }).start();

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(coordinatorResources.toString());

        // Request to other servers for getting specified resource
        if(result.toString().trim().length() == 0) {
            message = "Requested Resource of ID " + resourceID + " by coordinator " + coordinatorID + " is not found! so Coordinator is added to waiting queue!";
            activityLogger.log(" REQUEST RESOURCE (" + coordinatorID + ", " + resourceID + ", " + duration + ") " , " COMPLETED ", message);
            return message;
        }

        activityLogger.log(" REQUEST RESOURCE (" + coordinatorID + ", " + resourceID + ", " + duration + ") " , " COMPLETED ", message);
        return result.toString();

    }

    private String sendResourceRequest(final String location, String resourceID, int duration) {

        String recordCount = "";
        try {
            final InetAddress inetAddress = InetAddress.getByName("localhost");
//            activityLogger.log(location, " UPD MESSAGE SENT ", "UPD Message sent for Request Resource");
            final DatagramSocket socket = new DatagramSocket();
            byte[] data = (serverID + "-GET_RESOURCE" + "-" + resourceID + "-" +  duration).toString().getBytes();

            final DatagramPacket packet = new DatagramPacket(data, data.length, inetAddress, PortConstants.getUdpPort(location)) ;
            socket.send(packet);
            System.out.println("inside the request udp: "+ location + "   "+ data.toString());
            data = new byte[1000];

            DatagramPacket responsePacket = new DatagramPacket(data, data.length);
            socket.receive(responsePacket);
            recordCount = " " + new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
            System.out.println("after received the request udp: "+ location + "   "+ data.toString());

            socket.close();
        } catch (Exception e) {
        }
        return recordCount;
    }

    public String getRequestedResource(String id, int duration) {
        String message = "";
        for (HashMap<String, Resource> resourceMap : resources.values()) {
            Resource resource = resourceMap.get(id);

            if (resource != null) {
                if(resource.getDuration() >= duration) {
                    resource.setDuration(resource.getDuration() - duration);
                    message = "Resource of ID: " + id + " is borrowed successfully and duration is updated to " + resource.getDuration() + " in " + serverID + "server" ;
                    activityLogger.log("REQUEST RESOURCE", " COMPLETED " , message);
                    return message;
                }
            }
        }
        message = "Resource of ID: " + id + " is not available to borrow!";
        activityLogger.log("REQUEST RESOURCE", " FAILED " , message);
        return "";

    }

    public String findResource(String coordinatorID, String resourceName){
        // Retrieve the inner HashMap for the resource name (e.g., "AMBULANCE")
        final StringBuilder result =  new StringBuilder();
        String message = "";

//        boolean isUserIdValid = ValidationService.isUserIDValid(coordinatorID);
//        boolean isNameValid = ValidationService.isResourceNameValid(resourceName);
//
//        if(!(isNameValid && isUserIdValid)) {
//            message = "Input is not valid for : " + coordinatorID + " " + resourceName;
//            activityLogger.log(" FIND RESOURCE (" + coordinatorID + ", " + resourceName + ") " , " FAILED ", message);
//            return message;
//        }

        result.append(resourceName.toUpperCase());
        int startLen = result.length();

        HashMap<String, Resource> specifiedResources = resources.get(resourceName);
        if(specifiedResources != null) {
            if(coordinatorResources.containsKey(coordinatorID)) {
                for(Resource res: coordinatorResources.get(coordinatorID)) {
                    System.out.println("resID: " + res.getResourceID());
                    if(specifiedResources.containsKey(res.getResourceID())) {
                        result.append(" " + res.getResourceID() + "-" + res.getDuration());
                    }
                }
            }
        }


        CountDownLatch latch = new CountDownLatch(serverList.size() - 1);
        final StringBuilder response =  new StringBuilder();
        for(String server:  serverList) {

            if(!serverID.equalsIgnoreCase(server)) {
                new Thread(() -> {
                    final String count = sendResourceAvailabilityRequest(server, resourceName);
                    if(count != null)
                        synchronized(result) {
                            response.append(count);
                        }
                    latch.countDown();
                }).start();
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String responseString = response.toString().trim();
        String[] resources = responseString.split(" ");
        if (resources != null) {
            for (String resource : resources) {
                // Split each resource by '-'
                String[] parts = resource.split("-");

                if (parts.length == 2) {
                    String resourceID = parts[0];
                    String count = parts[1];

                    if(coordinatorResources.containsKey(coordinatorID)) {
                        for(Resource res: coordinatorResources.get(coordinatorID)) {
                            if(resourceID.equalsIgnoreCase(res.getResourceID())) {
                                result.append(" " + resourceID + "-" + res.getDuration());
                            }
                        }
                    }

                }
            }
        }
        if(startLen == result.length()) {
            message = "Resources of " + resourceName+ " occupied by coordinator " + coordinatorID + " are not found!";
            activityLogger.log(" FIND RESOURCE (" + coordinatorID +  ") " , " COMPLETED ", message);
            return message;
        }

        message = "Resources of " + resourceName+ " occupied by coordinator " + coordinatorID + " are returned successfully!";
        activityLogger.log(" FIND RESOURCE (" + coordinatorID +  ") " , " COMPLETED ", message);
        System.out.println(result);

        return result.toString();
    }

    public synchronized String returnResource(String coordinatorID, String resourceID){
        // Iterate through the outer HashMap to find the resource by its ID
        String message = "";
//        boolean isUserIdValid = ValidationService.isUserIDValid(coordinatorID);
//        boolean isIDValid = ValidationService.isResourceIDValid(resourceID);
//
//        if(!(isIDValid && isUserIdValid)) {
//            message = "Input is not valid for : " + coordinatorID + " " + resourceID;
//            activityLogger.log(" RETURN RESOURCE (" + coordinatorID + ", " + resourceID + ") " , " FAILED ", message);
//            return message;
//        }

//        for (HashMap<String, Resource> resourceMap : resources.values()) {
//            Resource resource = resourceMap.get(resourceID);
//            if (resource != null) {
        // Perform any logic to return the resource
        if(coordinatorResources.containsKey(coordinatorID)) {
            for(Resource res: coordinatorResources.get(coordinatorID)) {
                if(res.getResourceID().equalsIgnoreCase(resourceID)) {
                    message = "Resource " + resourceID + "-" + res.getDuration() + " returned successfully to coordinator " + coordinatorID;
                    activityLogger.log(" RETURN RESOURCE (" + coordinatorID + ", " + resourceID +  ") " , " COMPLETED ", message);
                    return message;
                }
            }
        }
//            }
//        }
        message = "Resource of ID: " + resourceID + " not found.";
        activityLogger.log(" RETURN RESOURCE (" + coordinatorID + ", " + resourceID +  ") " , " FAILED ", message);
        return message;
    }

    public synchronized String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        String message = "";
        int durationRequest = 0;
        String returnedResource = returnResource(coordinatorID, oldResourceID);
        if(returnedResource.contains("not found")) {
            message = "Coordinator of ID: " + coordinatorID +  " didn't acquired Resource of ID: " + oldResourceID + ".";
//            activityLogger.log(" SWAP RESOURCE (" + coordinatorID + ", " + oldResourceID +  ") " , " FAILED ", message);
            return message;
        }
        if(coordinatorResources.containsKey(coordinatorID)) {
            for(Resource res: coordinatorResources.get(coordinatorID)) {
                if(res.getResourceID().equals(oldResourceID)) {
                    durationRequest = res.getDuration();
                    coordinatorResources.get(coordinatorID).remove(res);
                    break;
                }
            }
        }

        String requestedResource = requestResource(coordinatorID, newResourceID, durationRequest);
        if(requestedResource.contains("not found")) {
            message = "Coordinator of ID: " + coordinatorID +  " couldn't acquire Resource of ID: " + newResourceID + ".";
            activityLogger.log(" SWAP RESOURCE (" + coordinatorID + ", " + oldResourceID +  ") " , " FAILED ", message);
            return message;
        }
        message = "Coordinator of ID: " + coordinatorID + " Successfully swapped old resource " + oldResourceID + " with new resource " + newResourceID;
//        activityLogger.log(" SWAP RESOURCE (" + coordinatorID + ", " + oldResourceID + ", " + oldResourceType + ", " + newResourceID + ", " + newResourceType + ", "+ ") ",
//                " COMPLETED ", message);

        return message;
    }

}
