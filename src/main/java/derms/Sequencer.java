package derms;

import derms.net.runicast.ReliableUnicastReceiver;
import derms.net.tomulticast.TotalOrderMulticastSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * The sequencer receives requests from the FE via {@link derms.net.runicast.ReliableUnicastReceiver} and
 * sends them to the RMs via {@link derms.net.tomulticast.TotalOrderMulticastSender}.
 */
public class Sequencer implements Runnable {
    public static final String usage = "Usage: java Sequencer <laddr>";

    private final ReliableUnicastReceiver<Request> in; // From FE.
    private final TotalOrderMulticastSender<Request> out; // To RMs.
    private final Logger log;

    /**
     *
     * @param laddr The local IP address.
     */
    public Sequencer(InetAddress laddr) throws IOException {
        this.in = new ReliableUnicastReceiver<Request>(new InetSocketAddress(laddr, Config.sequencerInPort));
        this.out = new TotalOrderMulticastSender<Request>(Config.group, laddr);
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
            seq = new Sequencer(args.laddr);
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

        private Args(String[] args) throws IllegalArgumentException {
            if (args.length < 1) {
                throw new IllegalArgumentException("Missing argument 'laddr'");
            }
            try {
                this.laddr = InetAddress.getByName(args[0]);
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad value of 'laddr': " + e.getMessage());
            }
        }
    }
}
