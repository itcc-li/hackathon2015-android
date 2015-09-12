package li.itcc.hackathon15.services;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import li.itcc.hackathon15.json.HttpStreamConnection;
import li.itcc.hackathon15.json.JSONArray;
import li.itcc.hackathon15.json.JSONObject;
import li.itcc.hackathon15.json.JSONTokener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiServices {
    private final String Url;
    private final Context Context;

    public PoiServices(Context context, String url) {
        this.Context = context;
        this.Url = url;
    }

    public PoiListBean getPoiList(PoiListQuery query) throws Exception {

        URL finalUrl = new URL(Url + "/api/index.php/pois");
        HttpStreamConnection connection = new HttpStreamConnection(Context, finalUrl);
        connection.setDoPost(false);
        //connection.
        connection.open();

        InputStream in = connection.execute();
        JSONTokener tokener = new JSONTokener(in);

        JSONArray jsonArray = new JSONArray(tokener);
        //JSONObject jsonObject = new JSONObject(jsonArray);

        PoiListBean result = new PoiListBean();
        PoiBean[] list = new PoiBean[jsonArray.length()];

        for (int i = 0; i < list.length; i++) {
            list[i] = convertPoiBean(jsonArray.getJSONObject(i));

        }

        result.setAllPolis(list);
        return result;
    }

    private PoiBean convertPoiBean(JSONObject jsonObject) throws Exception {

        PoiBean result = new PoiBean();
        result.setLatitude(jsonObject.getDouble("latitude"));
        result.setLongitude(jsonObject.getDouble("longitude"));
        result.setPoiName(jsonObject.getString("name"));
        //result.setLatitude(jsonObject.getDouble("latitude"));

        return  result;
    }

    public void savePoiDetails(PoiDetailBean param) throws Exception {
        //throw new IOException("TODO");
        Thread.sleep(1000);
    }
}
