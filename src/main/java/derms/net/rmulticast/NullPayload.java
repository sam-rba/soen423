package derms.net.rmulticast;

class NullPayload implements MessagePayload {
    @Override
    public int hash() {
        return -1;
    }
}
