package derms.net.rmulticast;

import java.net.InetAddress;

/** A message whose only purpose is to announce the presence of the sender in the group. */
class AnnounceMessage extends Message<NullPayload> {
    AnnounceMessage(InetAddress laddr) {
        super(new NullPayload(), laddr, new MessageID[0], new MessageID[0]);
    }
}
