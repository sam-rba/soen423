package derms.io;

import java.io.*;
import java.nio.ByteBuffer;

public class Serial {
    public static ByteBuffer encode(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
        oos.close();
        return buf;
    }

    public static <T extends Serializable> T decode(ByteBuffer buf, Class<T> clazz) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(buf.array()));
        T obj = clazz.cast(ois.readObject());
        ois.close();
        return obj;
    }
}
