package li.itcc.hackathon15.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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
            byte[] thumbRaw = Base64.decode(thumbBase64, Base64.NO_WRAP);
            result.setThumbnail(thumbRaw);
        }
        return  result;
    }

    public void savePoiDetails(PoiDetailBean param) throws Exception {
        URL finalUrl = new URL(Url + "/api/index.php/pois?fields=id,name,longitude,latitude,thumbnail");
        HttpStreamConnection connection = new HttpStreamConnection(Context, finalUrl);
        connection.setDoPost(true);
        OutputStream outbin = connection.open();
        OutputStreamWriter writer = new OutputStreamWriter(outbin, "UTF-8");
        PrintWriter pw = new PrintWriter(writer);
        FormWriter fw = new FormWriter(pw);
        fw.print("user_id", "1");
        fw.print("name", param.getPoiName());
        fw.print("description", param.getComment());
        fw.print("longitude", Double.toString(param.getLongitude()));
        fw.print("latitude", Double.toString(param.getLatitude()));
        //
        //Float ratingFloat = param.getRating();
        //if (ratingFloat != null) {
        //    pw.print("rating=");
        //    pw.println(ratingFloat.toString());
        //}
        //
        byte[] image = param.getImage();
        if (image != null) {
            String imageBase64 = Base64.encodeToString(image, Base64.NO_WRAP);
            fw.print("image", imageBase64);
        }
        pw.flush();
        pw.close();
        connection.execute();
        connection.close();
    }

    private void write(PrintWriter pw, String key, String value) {
        pw.print(key);
        pw.print("=");
        pw.print(value);
    }


    private static class FormWriter {
        private final PrintWriter fOut;
        private boolean fEmpty = true;

        public FormWriter(PrintWriter destination) {
            fOut = destination;
        }

        public void print(String key, String value) throws Exception {
            if (!fEmpty) {
                fOut.print("&");
            }
            fOut.print(key);
            fOut.print("=");
            String urlEncoded = URLEncoder.encode(value, "utf-8");
            fOut.print(urlEncoded);
            fEmpty = false;
        }
    }


}
