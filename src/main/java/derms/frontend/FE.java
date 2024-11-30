package derms.frontend;

import java.io.IOException;
import java.net.*;

import javax.xml.ws.Endpoint;

import derms.Config;
import  derms.Request;
import  derms.Response;
import derms.net.runicast.ReliableUnicastSender;

import java.util.concurrent.atomic.AtomicInteger;

//import constants.Constants;

public class FE {
    private static final int sequencerPort = 3333;
    private static final String sequencerIP = "localhost";
    private static ReliableUnicastSender<Request> sequencerSock;
    private static final String RM_Multicast_group_address = "230.1.1.10";
    private static final int FE_SQ_PORT = 1414;
    private static final int FE_PORT = 1999;
    private static final int RM_Multicast_Port = 1234;
    public static String FE_Address = "http://localhost:8067/"+DERMSInterface.class.getSimpleName();
    private static final String FE_IP_Address = "localhost";

    private static AtomicInteger sequenceIDGenerator = new AtomicInteger(0);
//    public static String FE_IP_Address = "localhost";

    public static void main(String[] args) {
        try {
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
                }

                @Override
                public void informRmIsDown(int RmNumber) {
//                    String errorMessage = new MyRequest(RmNumber, "2").toString();
                    Request errorMessage = new Request(RmNumber, "2");
                    System.out.println("Rm:" + RmNumber + "is down");
//                    sendMulticastFaultMessageToRms(errorMessage);
                    sendUnicastToSequencer(errorMessage);
                }

                @Override
                public int sendRequestToSequencer(Request myRequest) {
                    return sendUnicastToSequencer(myRequest);
                }

                @Override
                public void retryRequest(Request request) {
                    System.out.println("No response from all Rms, Retrying request...");
                    sendUnicastToSequencer(request);
                }
            };
            DERMSServerImpl servant = new DERMSServerImpl(inter);
            Endpoint endpoint = Endpoint.publish(FE_Address, servant);
            Runnable task = () -> {
                listenForUDPResponses(servant);
            };
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
//            Logger.serverLog(serverID, "Exception: " + e);
        } finally {
            try {
                sequencerSock.close();
            } catch (Exception e) {
                System.out.println("Error closing sequencer socket:");
                e.printStackTrace(System.out);
            }
        }

//        System.out.println("FrontEnd Server Shutting down");
//        Logger.serverLog(serverID, " Server Shutting down");
    }

    private static int sendUnicastToSequencer(Request requestFromClient) {
        int sequenceID = sequenceIDGenerator.incrementAndGet();
        try {
            sequencerSock.send(requestFromClient);

//            System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>> send is done");
//            aSocket.setSoTimeout(1000);
//            // Set up an UPD packet for recieving
//            byte[] buffer = new byte[1000];
//            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
//            // Try to receive the response from the ping
//            aSocket.receive(response);
//            String sentence = new String(response.getData(), 0,
//                    response.getLength());
//            System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>" + sentence);
//            sequenceID = Integer.parseInt(sentence.trim());
//            System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>SequenceID:" + sequenceID);

        } catch (SocketException e) {
            System.out.println("Failed: " + requestFromClient.noRequestSendError());
            System.out.println("Socket: " + e.getMessage());
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

    private static void listenForUDPResponses(DERMSServerImpl servant) {
        DatagramSocket aSocket = null;
        try {

//            aSocket = new MulticastSocket(1413);
//            InetAddress[] allAddresses = Inet4Address.getAllByName("SepJ-ROG");
            InetAddress desiredAddress = InetAddress.getByName(FE_IP_Address);
//            //In order to find the desired Ip to be routed by other modules (WiFi adapter)
//            for (InetAddress address :
//                    allAddresses) {
//                if (address.getHostAddress().startsWith("192.168.2")) {
//                    desiredAddress = address;
//                }
//            }
//            aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));
            aSocket = new DatagramSocket(FE_PORT, desiredAddress);
            byte[] buffer = new byte[1000];
            System.out.println("FE Server Started on " + desiredAddress + ":" + FE_PORT + "............");

            while (true) {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(response);
                String sentence = new String(response.getData(), 0,
                        response.getLength()).trim();
                System.out.println("FE:Response received from Rm>>>" + sentence);
                Response rmResponse = new Response(sentence);
//                String[] parts = sentence.split(";");

                System.out.println("Adding response to FrontEndImplementation:");
                servant.addReceivedResponse(rmResponse);
//                DatagramPacket reply = new DatagramPacket(response.getData(), response.getLength(), response.getAddress(),
//                        response.getPort());
//                aSocket.send(reply);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
//            if (aSocket != null)
//                aSocket.close();
        }
    }
}