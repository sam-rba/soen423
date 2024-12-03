package derms.test;

import derms.ReplicaManager;
import derms.Sequencer;
import derms.client.ResponderClient;
import derms.frontend.FE;
import derms.replica1.DERMSServerPublisher;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.*;
import java.util.*;
import derms.util.*;
import derms.replica3.*;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest {

    private static final String TEST_LOG_PATH = "SystemTest.log";
    private static final String EXPECTED_LOG_PATH_NORM = "TestExpected.log";
    private static final String EXPECTED_LOG_PATH_BYZ = "TestExpectedByz.log";
    private static final String EXPECTED_LOG_PATH_CRASH = "TestExpectedCrash.log";
    private static final String EXPECTED_LOG_PATH_COMBINED = "TestExpectedCombined.log";

    // [TODO]
    //  input IP and NET config
    private static String IP = "127.0.0.1";

    @BeforeEach
    void clearLogFile() throws IOException {
        TestLogger.clearLog();
    }

    @BeforeAll
    static void runMains() throws IOException {
        String[] argsFE = {IP, IP};

        Thread feThread = new Thread(() -> {
            try {
                FE.main(argsFE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        feThread.start();

        Thread sequencerThread = new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(IP);
                NetworkInterface netIfc = NetworkInterface.getByInetAddress(ip);
                Sequencer sequencer = new Sequencer(ip, netIfc);
                sequencer.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        sequencerThread.start();
    }

    @AfterEach
    void stopServers() {
        // Stop the DERMSServerPublisher
        DERMSServerPublisher.stop();
    }

    @Test
    void testNormal() throws IOException {
        // Replica 1
        String[] argsRM = {"1", "MTL", IP, "0", "0"};

        // [TODO]
        // Run the main function of the desired replica, for example:
        DERMSServerPublisher.main(new String[0]);

        ReplicaManager.main(argsRM);
        ResponderClient responderClient = new ResponderClient(IP);
        responderClient.addResource("MTL1001", "ambulance", 10);

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareFiles(TEST_LOG_PATH, EXPECTED_LOG_PATH_NORM));
    }

    @Test
    void testByzantine() throws IOException {
        // Replica 1
        String[] argsRM = {"1", "MTL", IP, "1", "0"};

        // [TODO]
        // Run the main function of the desired replica, for example:
        DERMSServerPublisher.main(new String[0]);

        ReplicaManager.main(argsRM);
        ResponderClient responderClient = new ResponderClient(IP);
        responderClient.addResource("MTL1001", "ambulance", 10);

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareFiles(TEST_LOG_PATH, EXPECTED_LOG_PATH_BYZ));
    }

    @Test
    void testCrash() throws IOException {
        // Replica 1
        String[] argsRM = {"1", "MTL", IP, "0", "1"};

        // [TODO]
        // Run the main function of the desired replica, for example:
        DERMSServerPublisher.main(new String[0]);

        ReplicaManager.main(argsRM);
        ResponderClient responderClient = new ResponderClient(IP);
        responderClient.addResource("MTL1001", "ambulance", 10);

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareFiles(TEST_LOG_PATH, EXPECTED_LOG_PATH_CRASH));
    }

    @Test
    void testCombined() throws IOException {
        // Replica 1 and 2
        String[] argsRM1 = {"1", "MTL", IP, "1", "0"};
        String[] argsRM3 = {"3", "MTL", IP, "0", "1"};

        // [TODO]
        // Run the main function of the desired TWO replicas, for example:
        DERMSServerPublisher.main(new String[0]);
        MTLServer.main(new String[0]);
        QUEServer.main(new String[0]);
        SHEServer.main(new String[0]);

        ReplicaManager.main(argsRM1);
        ReplicaManager.main(argsRM3);

        Thread thread1 = new Thread(() -> {
            ResponderClient responderClient = null;
            try {
                responderClient = new ResponderClient(IP);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (responderClient != null) {
                    responderClient.addResource("MTL1001", "ambulance", 10);
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            ResponderClient responderClient2 = null;
            try {
                responderClient2 = new ResponderClient(IP);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {
            if (responderClient2 != null) {
                responderClient2.addResource("MTL1002", "ambulance", 11);
            }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            thread2.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareFiles(TEST_LOG_PATH, EXPECTED_LOG_PATH_COMBINED));
    }
}