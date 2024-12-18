package derms.net.rmulticast;

import java.io.Serializable;

class MessageID implements Serializable {
    int id;

    MessageID(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        MessageID other = (MessageID) obj;
        return other.id == this.id;
    }

    @Override
    public String toString() {
        return ""+id;
    }
}
