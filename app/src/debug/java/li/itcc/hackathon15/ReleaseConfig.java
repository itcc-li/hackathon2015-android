package li.itcc.hackathon15;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Arthur on 17.09.2015.
 */
public class ReleaseConfig {
    private static final String DEBUG_SERVER = "192.168.160.2";
    public static final String URL = "http://" + DEBUG_SERVER + ":8080";

    /**
     * For local testing only
     * @param urlStr
     * @return
     */
    public static String dnsHack(String urlStr) {
        String debugServer = System.getProperty("debugServer");
        if (debugServer == null) {
            debugServer = DEBUG_SERVER;
        }
        try {
            URL oldUrl = new URL(urlStr);
            URL newURL = new URL(oldUrl.getProtocol(), debugServer, oldUrl.getPort(), oldUrl.getFile());
            return newURL.toString();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urlStr;
    }
}
