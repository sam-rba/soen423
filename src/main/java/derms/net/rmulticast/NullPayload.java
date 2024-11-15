package derms.net.rmulticast;

import java.io.Serializable;

class NullPayload implements Serializable, Hashable {
    @Override
    public int hash() {
        return -1;
    }
}
