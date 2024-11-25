package derms;

import java.net.InetSocketAddress;

public class Config {
    // The port where the sequencer listens for requests from the FE.
    public static final int sequencerInPort = 62310;

    // The multicast group of the RMs and sequencer.
    public static final InetSocketAddress group = new InetSocketAddress("225.5.5.6", 62311);
}
