package derms.net.rmulticast;

class MessageID {
    int id;

    MessageID(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
