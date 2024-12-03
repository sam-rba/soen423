package derms.replica3;

//import logger.Logger;

import derms.City;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import javax.xml.ws.Endpoint;
//
//import constants.Constants;
//import constants.PortConstants;
public class QUEServer {
    public static void main(String[] args) {

        try{
            final Logger activityLogger = new Logger("QUEServer.log");
            int udpPort = PortConstants.getUdpPort("QUE");
            City ct = new City("QUE");
            /* Create servant and register it with the ORB */
            final Replica3 ServerImpl = new Replica3(ct, null);
//            Endpoint endpoint = Endpoint.publish(Constants.QUEBEC_ADDRESS, ServerImpl);

            /*Start UDP server for QUE*/
            new Thread(() -> {
                startUdpServer(activityLogger, ServerImpl, udpPort);
            }).start();

            System.out.println("#=== QUE Server is started ===#");

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }

    private static void startUdpServer(final Logger activityLogger, final Replica3 server, int udpPort) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(udpPort);
            while (true) {
                try {
                    final byte[] data = new byte[1000];
                    final DatagramPacket packet = new DatagramPacket(data, data.length);
                    socket.receive(packet);
                    System.out.println("request received" + udpPort);

                    new Thread(() -> {
                        processRequest(activityLogger, server, packet);
                    }).start();
                } catch (IOException e) {
                    activityLogger.log("", "EXCEPTION", e.getMessage());
                }
            }
        } catch (SocketException e1) {
            activityLogger.log("", "EXCEPTION", e1.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static void processRequest(final Logger activityLogger, final Replica3 server, final DatagramPacket packet) {
        byte[] response;
        DatagramSocket socket = null;
        final String request = new String(packet.getData(), StandardCharsets.UTF_8).trim();
        final String[] packetData = request.split("-");
        final String sourceServer = packetData[0].trim();
        final String action = packetData[1].trim();
        final String resourceName = packetData[2].trim();
        String resourceID = "";
        int duration = 0;

        if(packetData.length == 4) {
            resourceID = packetData[2].trim();
            duration = Integer.parseInt(packetData[3]);
        }

        try {
            socket = new DatagramSocket();

            if("GET_AVAILABLE_RESOURCES".equalsIgnoreCase(action)) {
                response = sanitizeXml(server.getAvailableResources(resourceName).toString()).getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort()));
            }else if("GET_RESOURCE".equalsIgnoreCase(action)) {
                response = server.getRequestedResource(resourceID, duration).toString().getBytes();
                socket.send(new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort()));
            }

            activityLogger.log("", action, "Response sent");
        } catch (IOException e) {
            activityLogger.log("", "EXCEPTION", e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static String sanitizeXml(String input) {
        if (input == null) {
            return ""; // or handle as needed
        }

        return input.replaceAll("[\\x00]", "");
    }

}
