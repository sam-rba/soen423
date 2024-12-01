package derms;

import derms.net.runicast.ReliableUnicastReceiver;
import derms.net.tomulticast.TotalOrderMulticastSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.logging.Logger;

/**
 * The sequencer receives requests from the FE via {@link derms.net.runicast.ReliableUnicastReceiver} and
 * sends them to the RMs via {@link derms.net.tomulticast.TotalOrderMulticastSender}.
 */
public class Sequencer implements Runnable {
    public static final String usage = "Usage: java Sequencer <ip address> <network interface>";

    private final ReliableUnicastReceiver<Request> in; // From FE.
    private final TotalOrderMulticastSender<Request> out; // To RMs.
    private final Logger log;

    /**
     *
     * @param laddr The local IP address.
     * @param ifs The network interface to use.
     */
    public Sequencer(InetAddress laddr, NetworkInterface ifs) throws IOException {
        this.in = new ReliableUnicastReceiver<Request>(new InetSocketAddress(laddr, Config.sequencerInPort));
        this.out = new TotalOrderMulticastSender<Request>(Config.group, laddr, ifs);
        this.log = Logger.getLogger(getClass().getName());
    }

    public static void main(String[] cmdlineArgs) {
        Args args = null;
        try {
            args = new Args(cmdlineArgs);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(usage);
            System.exit(1);
        }

        Sequencer seq = null;
        try {
            seq = new Sequencer(args.laddr, args.ifs);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            seq.run();
        } finally {
            try {
                seq.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    public void close() throws IOException {
        in.close();
        out.close();
    }

    @Override
    public void run() {
        for (;;) {
            try {
                Request req = in.receive();
                System.out.println("Seq: received: " + req);
                out.send(req);
            } catch (InterruptedException e) {
                log.info("Shutting down.");
                return;
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private static class Args {
        private final InetAddress laddr;
        private final NetworkInterface ifs;

        private Args(String[] args) throws IllegalArgumentException {
            if (args.length < 1) {
                throw new IllegalArgumentException("Missing argument 'ip address'");
            }
            try {
                this.laddr = InetAddress.getByName(args[0]);
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad value of 'ip address': " + e.getMessage());
            }

            if (args.length < 2) {
                throw new IllegalArgumentException("Missing argument 'network interface'");
            }
            try {
                this.ifs = NetworkInterface.getByName(args[1]);
            } catch (Exception e ) {
                throw new IllegalArgumentException("Bad value of 'network interface': " + e.getMessage());
            }
        }
    }
}
