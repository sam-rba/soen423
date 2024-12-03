package derms.frontend;

import java.io.IOException;
import java.net.*;

import javax.xml.ws.Endpoint;

import derms.Config;
import derms.Request;
import derms.Response;
import derms.net.runicast.ReliableUnicastReceiver;
import derms.net.runicast.ReliableUnicastSender;
import derms.util.TestLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//import constants.Constants;

public class FE {
    public static final String usage = "Usage: java FE <FE IP> <Sequencer IP>";
    private static String frontendIP;
    private static String sequencerIP;
    private static ReliableUnicastSender<Request> sequencerSock;
    private static final String RM_Multicast_group_address = Config.group.toString();
    private static final int RM_Multicast_Port = 1234;
    private static AtomicInteger sequenceIDGenerator = new AtomicInteger(0);
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println(usage);
                return;
            }
            frontendIP = args[0];
            sequencerIP = args[1];
            System.out.println("Connecting to sequencer ("
                    + sequencerIP + ":" + Config.sequencerInPort + ")...");
            sequencerSock = new ReliableUnicastSender<Request>(
                    new InetSocketAddress(sequencerIP, Config.sequencerInPort));

            FEInterface inter = new FEInterface() {
                @Override
                public void informRmHasBug(int RmNumber) {
//                    String errorMessage = new MyRequest(RmNumber, "1").toString();
                    Request errorMessage = new Request(RmNumber, "1");
                    System.out.println("Rm:" + RmNumber + "has bug");
//                    sendMulticastFaultMessageToRms(errorMessage);
                    sendUnicastToSequencer(errorMessage);
                    TestLogger.log("FE: {BYZANTINE: REPLICA" + RmNumber + "}");
                }

                @Override
                public void informRmIsDown(int RmNumber) {
//                    String errorMessage = new MyRequest(RmNumber, "2").toString();
                    Request errorMessage = new Request(RmNumber, "2");
                    System.out.println("Rm:" + RmNumber + "is down");
//                    sendMulticastFaultMessageToRms(errorMessage);
                    sendUnicastToSequencer(errorMessage);
                    TestLogger.log("FE: {CRASH: REPLICA" + RmNumber + "}");
                }

                @Override
                public int sendRequestToSequencer(Request myRequest) {
                    int r = sendUnicastToSequencer(myRequest);
                    System.out.println("request: " + myRequest + " returned " + r);
                    return r;
                }

                @Override
                public void retryRequest(Request request) {
                    System.out.println("No response from all Rms, Retrying request...");
                    sendUnicastToSequencer(request);
                }
            };
            DERMSServerImpl servant = new DERMSServerImpl(inter);
            Endpoint endpoint = Endpoint.publish(endpointURL(frontendIP), servant);

            Runnable task = () -> {
                try {
                    listenForUDPResponses(servant);
                    sequencerSock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
//            Logger.serverLog(serverID, "Exception: " + e);
        }

//        System.out.println("FrontEnd Server Shutting down");
//        Logger.serverLog(serverID, " Server Shutting down");
    }

    // The URL where the web service endpoint is published.
    public static String endpointURL(String frontendHost) {
        return "http://" + frontendHost + ":" + Config.frontendEndpointPort + "/" + DERMSInterface.class.getSimpleName();
    }

    private static int sendUnicastToSequencer(Request requestFromClient) {
        int sequenceID = sequenceIDGenerator.incrementAndGet();
        requestFromClient.setId(sequenceID);
        try {
            sequencerSock.send(requestFromClient);
        } catch (IOException e) {
            System.out.println("Failed: " + requestFromClient.noRequestSendError());
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        }
        return sequenceID;
    }

    public static void sendMulticastFaultMessageToRms(String errorMessage) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] messages = errorMessage.getBytes();
            InetAddress aHost = InetAddress.getByName(RM_Multicast_group_address);

            DatagramPacket request = new DatagramPacket(messages, messages.length, aHost, RM_Multicast_Port);
            System.out.println("FE:sendMulticastFaultMessageToRms>>>" + errorMessage);
            aSocket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private static void listenForUDPResponses(DERMSServerImpl servant) {
//        DatagramSocket aSocket = null;
//        try {
//
////            aSocket = new MulticastSocket(1413);
////            InetAddress[] allAddresses = Inet4Address.getAllByName("SepJ-ROG");
//            InetAddress desiredAddress = InetAddress.getByName(FE_IP_Address);
////            //In order to find the desired Ip to be routed by other modules (WiFi adapter)
////            for (InetAddress address :
////                    allAddresses) {
////                if (address.getHostAddress().startsWith("192.168.2")) {
////                    desiredAddress = address;
////                }
////            }
////            aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));
//            aSocket = new DatagramSocket(FE_PORT, desiredAddress);
//            byte[] buffer = new byte[1000];
//            System.out.println("FE Server Started on " + desiredAddress + ":" + FE_PORT + "............");
//
//            while (true) {
//                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
//                aSocket.receive(response);
//                String sentence = new String(response.getData(), 0,
//                        response.getLength()).trim();
//                System.out.println("FE:Response received from Rm>>>" + sentence);
//                Response rmResponse = new Response(sentence);
////                String[] parts = sentence.split(";");
//
//                System.out.println("Adding response to FrontEndImplementation:");
//                servant.addReceivedResponse(rmResponse);
////                DatagramPacket reply = new DatagramPacket(response.getData(), response.getLength(), response.getAddress(),
////                        response.getPort());
////                aSocket.send(reply);
//            }
//
//        } catch (SocketException e) {
//            System.out.println("Socket: " + e.getMessage());
//        } catch (IOException e) {
//            System.out.println("IO: " + e.getMessage());
//        } finally {

    /// /            if (aSocket != null)
    /// /                aSocket.close();
//        }
//    }
    private static void listenForUDPResponses(DERMSServerImpl servant) {
        List<ReliableUnicastReceiver<Response>> receivers = new ArrayList<ReliableUnicastReceiver<Response>>();
        // Initialize a receiver for each RM.
        for (int port : Config.frontendResponsePorts) {
            try {
                receivers.add(new ReliableUnicastReceiver<Response>(
                        new InetSocketAddress(frontendIP, port)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("FE listening for responses on " + frontendIP + ":" + port + "...");
        }
        for (ReliableUnicastReceiver<Response> receiver : receivers) {
            executor.execute(() -> {
                try {
                    while (true) {
                        // Receive a response from each RM.

                        // Blocking call to receive a message from RM
                        Response response;

                        response = receiver.receive();
                        System.out.println("FE: Response received from RM >>> " + response);

                        // Process the received response and add it to the servant
                        System.out.println("Adding response to FrontEndImplementation:");
                        servant.addReceivedResponse(response);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Listener interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        receiver.close();
                        System.out.println("ReliableUnicastReceiver closed.");
                    } catch (IOException e) {
                        System.out.println("Error closing ReliableUnicastReceiver: " + e.getMessage());
                    }
                }
            });
        }
        try {
            executor.awaitTermination(100, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}