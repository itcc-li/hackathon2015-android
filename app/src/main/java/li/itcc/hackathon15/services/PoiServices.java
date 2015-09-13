package li.itcc.hackathon15.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

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
        URL finalUrl = new URL(Url + "/api/index.php/pois?fields=id,name,longitude,latitude,thumbnail");
        HttpStreamConnection connection = new HttpStreamConnection(Context, finalUrl);
        connection.setDoPost(false);
        connection.open();

        InputStream in = connection.execute();
        JSONTokener tokener = new JSONTokener(in);
        JSONArray jsonArray = new JSONArray(tokener);
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
        result.setId(jsonObject.getLong("id"));
        result.setLatitude(jsonObject.getDouble("latitude"));
        result.setLongitude(jsonObject.getDouble("longitude"));
        result.setPoiName(jsonObject.getString("name"));
        String thumbBase64 = jsonObject.getString("thumbnail");
        if (thumbBase64 != null && thumbBase64.length() > 0) {
            byte[] thumbRaw = Base64.decode(thumbBase64, Base64.DEFAULT);
            result.setThumbnail(thumbRaw);
        }
        return  result;
    }

    public void savePoiDetails(PoiDetailBean param) throws Exception {

        String poiName = "name=" + param.getPoiName();
        String comment = "description=" + param.getComment();
        String longitude = "longitude=" + param.getLongitude();
        String latitude = "latitude=" + param.getLatitude();

        String pushString = "user_id=2&" + poiName + "&" + comment + "&" + longitude + "&" + latitude;

        URL finalUrl = new URL(Url);
        HttpStreamConnection connection = new HttpStreamConnection(Context, finalUrl);
        connection.setDoPost(true);
        connection.open();



        connection.execute();

        //var pushJson = "user_id=2&name=\(name)&description=\(description)&longitude=\(longitude)&latitude=\(latitude)"

        Log.d("pushserver", pushString);

        //throw new IOException("TODO");
        //Thread.sleep(1000);
    }


}
