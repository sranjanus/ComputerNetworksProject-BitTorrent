import java.io.*;

/**
 * Created by sarathfrancis on 4/17/16.
 */


/**
 * This class is used to convert a byte array to Object and vice versa(Taken logic from stack overflow)
 *
 */

public class ObjectToByteArrayandBack {

    public synchronized static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public synchronized static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
