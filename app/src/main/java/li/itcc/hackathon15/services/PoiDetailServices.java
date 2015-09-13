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

    public PoiFullDetailBean loadFullDetails(long id) throws Exception {
        URL finalUrl = new URL(Url + "/api/index.php/pois/"+id+"?fields=name,description,image,longitude,latitude,rating");
        HttpStreamConnection connection = new HttpStreamConnection(Context, finalUrl);
        connection.setDoPost(false);
        connection.open();
        InputStream in = connection.execute();
        JSONTokener tokener = new JSONTokener(in);
        JSONObject element = new JSONObject(tokener);
        PoiFullDetailBean result = convertPoiFullBean(element);
        return result;
    }

    private PoiFullDetailBean convertPoiFullBean(JSONObject jsonObject) throws Exception {
        PoiFullDetailBean result = new PoiFullDetailBean();
        result.setLatitude(jsonObject.getDouble("latitude"));
        result.setLongitude(jsonObject.getDouble("longitude"));
        result.setPoiName(jsonObject.getString("name"));
        result.setDescription(jsonObject.getString("description"));
        result.setRating((float) jsonObject.getDouble("rating"));
        String image64 = jsonObject.getString("image");
        if (image64 != null && image64.length() > 0) {
            byte[] img = Base64.decode(image64, Base64.NO_WRAP);
            result.setImage(img);
        }
        return  result;
    }

}
