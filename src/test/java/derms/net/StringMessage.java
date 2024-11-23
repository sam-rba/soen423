package derms.net;

public class StringMessage implements MessagePayload {
    String s;

    public StringMessage(String s) {
        this.s = s;
    }

    @Override
    public int hash() {
        return s.hashCode();
    }

    @Override
    public int hashCode() {
        return hash();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        StringMessage other = (StringMessage) obj;
        return other.s.equals(this.s);
    }

    @Override
    public String toString() {
        return s;
    }
}
