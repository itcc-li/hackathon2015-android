
package li.itcc.hackathon15.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



// see also http://developer.android.com/training/basics/network-ops/connecting.html

public class HttpStreamConnection {
    private URL fURL;
    private HttpURLConnection fConnection;
    private static String sfUserAgent;
    private InputStream fInputStream;
    private Context fContext;
    private int fReadTimeoutSecs;

    static {
        sfUserAgent = "Android";
    }

    private boolean fDoPost;


    public HttpStreamConnection(Context context, URL url) {
        fContext = context;
        fURL = url;
    }

    public void setDoPost(boolean doPost) {
        fDoPost = doPost;
    }

    public void setReadTimeoutSecs(int readTimeout) {
        fReadTimeoutSecs = readTimeout;
    }

    public OutputStream open() throws Exception {
        if (!isNetworkAvailable()) {
            throw new IOException("network unavailable");
        }
        fInputStream = null;
        URL url = fURL;
        //SocketAddress addr = new InetSocketAddress("172.16.6.122", 8081);
        //Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
        //fConnection = (HttpURLConnection)url.openConnection(proxy);
        //fConnection.setRequestMethod("");
        fConnection = (HttpURLConnection)url.openConnection();
        int readTimeoutMS;
        if (fReadTimeoutSecs > 0) {
            readTimeoutMS = fReadTimeoutSecs * 1000;
        }
        else {
            readTimeoutMS = 20000; // default to 20 sec
        }
        fConnection.setReadTimeout(readTimeoutMS);
        fConnection.setConnectTimeout(30000 /* milliseconds */);
        fConnection.setDoInput(true);
        if (fDoPost) {
            fConnection.setDoOutput(true);
            fConnection.setRequestMethod("POST");
        }
        fConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        fConnection.setUseCaches(false);
        fConnection.setRequestProperty("HOST", url.getHost());
        fConnection.setRequestProperty("User-Agent", sfUserAgent);
        fConnection.setRequestProperty("Accept", "application/json"); //to set Accept headers to 'application/json'

        // open the output
        if (fDoPost) {
            OutputStream out = fConnection.getOutputStream();
            return out;
        }
            return null;
    }

    public InputStream execute() throws Exception {
        fConnection.connect();
        int code = fConnection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Connect faild with code " + code);
        }
        fInputStream = fConnection.getInputStream();
        return fInputStream;
    }

    public String getProperty(String key) {
        return fConnection.getHeaderField(key);
    }

    public void close() throws Exception {
        if (fInputStream != null) {
            InputStream tmp = fInputStream;
            fInputStream = null;
            tmp.close();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)fContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        return activeNetworkInfo.isConnected();
    }

}
