//package derms;
//
////import derms.Replica3pkg.ResponderClient;
//import java.io.*;
//import java.util.*;
//
//import derms.Replica4pkg.RemoteServer;
//
//public class Replica4 implements Replica {
//
//    private ReplicaManager replicaManager;
//    private RemoteServer remoteServer;
//    private boolean alive = true;
//
//    public Replica4(ReplicaManager replicaManager){
//        this.replicaManager = replicaManager;
//    }
//
//    @Override
//    public boolean isAlive() {
//        return alive;
//    }
//
//    @Override
//    public void startProcess() {
//        this.remoteServer = new RemoteServer();
//        System.out.println("[Replica 4] Process started.");
//    }
//
//    @Override
//    public void processRequest(Request request) {
//        ResponderClient responderClient;
//        CoordinatorClient coordinatorClient;
//        String responseMessage;
//        switch (request.getFunction()) {
//            case "addResource":
//                responderClient = new ResponderClient(request.getClientID());
//                responseMessage = responderClient.addResource(request.getResourceID(), request.getResourceType(), request.getDuration());
//                break;
//            case "removeResource":
//                responderClient = new ResponderClient(request.getClientID());
//                responseMessage = responderClient.removeResource(request.getResourceID(), request.getDuration());
//                break;
//            case "listResourceAvailability":
//                responderClient = new ResponderClient(request.getClientID());
//                responseMessage = responderClient.listResourceAvailability(request.getResourceType());
//                break;
//            case "requestResource":
//                coordinatorClient = new CoordinatorClient(request.getClientID());
//                responseMessage = coordinatorClient.requestResource(request.getResourceID(), request.getDuration());
//                break;
//            case "findResource":
//                coordinatorClient = new CoordinatorClient(request.getClientID());
//                responseMessage = coordinatorClient.findResource(request.getResourceType());
//                break;
//            case "returnResource":
//                coordinatorClient = new CoordinatorClient(request.getClientID());
//                responseMessage = coordinatorClient.returnResource(request.getResourceID());
//                break;
//            case "swapResource":
//                coordinatorClient = new CoordinatorClient(request.getClientID());
//                responseMessage = coordinatorClient.swapResource(
//                        request.getClientID(),
//                        request.getOldResourceID(),
//                        request.getOldResourceType(),
//                        request.getResourceID(),
//                        request.getResourceType()
//                );
//                break;
//            default:
//                responseMessage = "Unrecognized function: " + request.getFunction();
//                log("Unrecognized function in request: " + request.getFunction());
//                break;
//        }
//
//        Response response = new Response(request.getSequenceNumber(), responseMessage);
//        log("Replica " + 4 + " processed request: " + request + ", response: " + response);
//        replicaManager.sendResponseToFE(response);
//    }
//
//    @Override
//    public void restart() {
//        shutDown();
//        startProcess();
//    }
//
//    public void shutDown(){
//        this.remoteServer.stopServers();
//    }
//
//    @Override
//    public int getId() {
//        return 4;
//    }
//
//    public synchronized void log(String message) {
//        String logMessage = new Date() + " - " + message;
//        System.out.println(logMessage);
//
//        try (FileWriter fw = new FileWriter("Replica4_log.txt", true);
//             BufferedWriter bw = new BufferedWriter(fw);
//             PrintWriter out = new PrintWriter(bw)) {
//            out.println(logMessage);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}