package derms.net.rmulticast;

import derms.net.MessagePayload;

class NullPayload implements MessagePayload {
    @Override
    public int hash() {
        return -1;
    }

    @Override
    public String toString() { return getClass().getSimpleName(); }
}
