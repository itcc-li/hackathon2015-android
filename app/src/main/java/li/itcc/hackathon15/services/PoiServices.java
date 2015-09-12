package li.itcc.hackathon15.services;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import li.itcc.hackathon15.json.HttpStreamConnection;
import li.itcc.hackathon15.json.JSONTokener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiServices {
    private final String fUrl;
    private final Context fContext;

    public PoiServices(Context context, String url) {
        fContext = context;
        fUrl = url;
    }

    public PoiListBean getPoiList(PoiListQuery query) throws Exception {
        URL finalUrl = null; // TODO
        HttpStreamConnection connection = new HttpStreamConnection(fContext, finalUrl);
        OutputStream out = connection.open();
        // out.write..
        InputStream in = connection.execute();
        JSONTokener tokener = new JSONTokener(in);
        // consume tokens.....
        throw new IOException("TODO");
    }

}
