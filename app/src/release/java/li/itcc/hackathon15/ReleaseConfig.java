package li.itcc.hackathon15;

/**
 * Created by Arthur on 17.09.2015.
 */
public class ReleaseConfig {
    public static final String URL = "https://flypostr.appspot.com:8080/";

    public static String dnsHack(String url) {
        // no hack in release mode
        return url;
    }
}
