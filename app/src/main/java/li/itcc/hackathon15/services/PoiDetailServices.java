package li.itcc.hackathon15.services;

import android.content.Context;
import android.util.Base64;

import java.io.InputStream;
import java.net.URL;

import li.itcc.hackathon15.json.HttpStreamConnection;
import li.itcc.hackathon15.json.JSONArray;
import li.itcc.hackathon15.json.JSONObject;
import li.itcc.hackathon15.json.JSONTokener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailServices {
    private final String Url;
    private final Context Context;

    public PoiDetailServices(Context context, String url) {
        this.Context = context;
        this.Url = url;
    }

    public PoiFullDetailBean loadFullDetails(long l) throws Exception {
        Thread.sleep(1000);
        PoiFullDetailBean result = new PoiFullDetailBean();
        return result;
    }
}
