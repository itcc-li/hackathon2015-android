package li.itcc.hackathon15;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Arthur on 17.09.2015.
 */
public class ReleaseConfig {

    /**
     * For local testin only
     * @param urlStr
     * @return
     */
    public static String dnsHack(String urlStr) {
        String localServer = System.getProperty("localServer");
        if (localServer == null) {
            localServer = "192.168.160.2";
        }
        try {
            URL oldUrl = new URL(urlStr);
            URL newURL = new URL(oldUrl.getProtocol(), localServer, oldUrl.getPort(), oldUrl.getFile());
            return newURL.toString();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urlStr;
    }
}
