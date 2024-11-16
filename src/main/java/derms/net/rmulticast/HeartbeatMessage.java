package derms.net.rmulticast;

import java.net.InetAddress;

/** A message whose only purpose is to propagate acknowledgements and process IDs to the group. */
class HeartbeatMessage extends Message<NullPayload> {
    HeartbeatMessage(InetAddress sender, MessageID[] acks, MessageID[] nacks) {
        super(new NullPayload(), sender, acks, nacks);
    }
}
