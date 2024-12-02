package derms.replica3;

public class PortConstants {

    public static final int MTL_PORT = 12012;
    public static final int QUE_PORT = 12013;
    public static final int SHE_PORT = 12014;

    public static final int MTL_UDP_PORT = 13013;
    public static final int QUE_UDP_PORT = 13014;
    public static final int SHE_UDP_PORT = 13015;

    public static int getUdpPort(final String serverLocation) {
        if(Constants.MONTREAL.equalsIgnoreCase(serverLocation)) {
            return MTL_UDP_PORT;
        } else if(Constants.QUEBEC.equalsIgnoreCase(serverLocation)) {
            return QUE_UDP_PORT;
        } else if(Constants.SHERBROOKE.equalsIgnoreCase(serverLocation)) {
            return SHE_UDP_PORT;
        }
        return 0;
    }

}
