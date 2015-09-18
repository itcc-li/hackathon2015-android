package li.itcc.hackathon15;

/**
 * Created by Arthur on 17.09.2015.
 */
public class ReleaseConfig {

    /**
     * For local testin only
     * @param url
     * @return
     */
    public static String dnsHack(String url) {
        url = url.replaceAll("NetCatDev", "192.168.160.2");
        return url;
    }
}
