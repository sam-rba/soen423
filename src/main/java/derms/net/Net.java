package derms.net;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class Net {
    /** Return the first non-loopback multicast interface in the system, or throw exception if no such interface exists. */
    public static NetworkInterface getMulticastInterface() throws SocketException, NoSuchElementException {
        Enumeration<NetworkInterface> ifss =  NetworkInterface.getNetworkInterfaces();
        while (ifss.hasMoreElements()) {
            NetworkInterface ifs = ifss.nextElement();
            if (ifs.supportsMulticast() && !ifs.isLoopback() && ifs.isUp())
                return ifs;
        }
        throw new NoSuchElementException("no multicast interface available");
    }
}
