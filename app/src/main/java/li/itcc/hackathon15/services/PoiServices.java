package li.itcc.hackathon15.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;
import android.util.Base64;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import li.itcc.hackathon15.json.HttpStreamConnection;
import li.itcc.hackaton15.backend.poiApi.PoiApi;
import li.itcc.hackaton15.backend.poiApi.model.PoiOverviewListBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiServices {
    private static PoiApi sfPoiApiService = null;
    private final String Url;
    private final Context Context;

    public PoiServices(Context context, String url) {
        this.Context = context;
        this.Url = url;

        if(sfPoiApiService == null) {
            PoiApi.Builder builder = new PoiApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl(url + "/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            sfPoiApiService = builder.build();
        }
    }

    public PoiOverviewListBean getPoiList(PoiOverviewQuery query) throws Exception {
        return sfPoiApiService.getPoiOverviewList(query.getLatitude(), query.getLongitude(), query.getMaxCount()).execute();
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
        Float ratingFloat = param.getRating();
        if (ratingFloat != null) {
            pw.print("rating=");
            pw.println(ratingFloat.toString());
        }
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
