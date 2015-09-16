package li.itcc.hackathon15.services;

import java.io.IOException;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import li.itcc.hackathon15.backend.poiApi.PoiApi;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateResultBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewListBean;


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

    public PoiCreateResultBean insertPoi(PoiCreateBean newPoi) throws Exception {
        return sfPoiApiService.insertPoi(newPoi).execute();
    }

}
