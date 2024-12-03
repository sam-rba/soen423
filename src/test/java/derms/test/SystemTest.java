package derms.test;

import derms.ReplicaManager;
import derms.Sequencer;
import derms.client.ResponderClient;
import derms.frontend.FE;
import derms.replica1.DERMSServerPublisher;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import derms.util.*;
import derms.replica3.*;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest {

    private static final String TEST_LOG_PATH = "SystemTest.log";
    private static final String EXPECTED_LOG_PATH = "TestExpected.log";

    // [TODO]
    //  input IP and NET config
    private static String IP = "127.0.0.1";
    private static String NET = "en0";

    @BeforeEach
    void clearLogFile() throws IOException {
        TestLogger.clearLog();
    }

    @BeforeAll
    static void runMains() throws IOException {
        String[] argsFE = {IP, IP};
        String[] argsSQ = {IP, NET};

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
                Sequencer.main(argsSQ);
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
        ResponderClient.Add addCommand = responderClient.new Add();
        addCommand.add("MTL1001", "ambulance", "10");

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareLineCounts(TEST_LOG_PATH, EXPECTED_LOG_PATH));
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
        ResponderClient.Add addCommand = responderClient.new Add();
        addCommand.add("MTL1001", "ambulance", "10");

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareLineCounts(TEST_LOG_PATH, EXPECTED_LOG_PATH));
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
        ResponderClient.Add addCommand = responderClient.new Add();
        addCommand.add("MTL1001", "ambulance", "10");

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareLineCounts(TEST_LOG_PATH, EXPECTED_LOG_PATH));
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

        ResponderClient responderClient = new ResponderClient(IP);
        ResponderClient.Add addCommand = responderClient.new Add();
        addCommand.add("MTL1001", "ambulance", "10");

        ResponderClient responderClient2 = new ResponderClient(IP);
        ResponderClient.Add addCommand2 = responderClient2.new Add();
        addCommand2.add("MTL1002", "ambulance", "11");

        // Compare the number of lines in the log files, to determine if they match or not
        assertTrue(LogComparator.compareLineCounts(TEST_LOG_PATH, EXPECTED_LOG_PATH));
    }
}