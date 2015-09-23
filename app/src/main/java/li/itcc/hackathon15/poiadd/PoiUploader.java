package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.RandomAccessFile;

import android.content.Context;

import li.itcc.hackathon15.ReleaseConfig;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.util.loading.GenericTask;
import li.itcc.hackathon15.util.loading.TaskExecutionListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiUploader {
    private final Context fContext;
    private final PoiUploadListener fListener;
    private File fLocalImageFile;

    public void setLocalImageFile(File localImageFile) {
        fLocalImageFile = localImageFile;
    }

    public interface PoiUploadListener extends TaskExecutionListener<PoiOverviewBean> {

    }

    public PoiUploader(Context context, PoiUploadListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void save(PoiCreateBean bean) {
        new SaveTask(fListener).execute(bean);
    }

    private class SaveTask extends GenericTask<PoiCreateBean, PoiOverviewBean> {

        public SaveTask(PoiUploadListener listener) {
            super(listener);
        }

        @Override
        protected PoiOverviewBean doInBackgroundOrThrow(PoiCreateBean... params) throws Exception {
            RandomAccessFile r = null;
            try {
                PoiCreateBean param = params[0];
                PoiServices poiServices = new PoiServices(fContext, ReleaseConfig.URL);
                // 1. upload the image file and get blob key
                File imageFile = fLocalImageFile;
                ImageUploader uploader = new ImageUploader(poiServices, this);
                String imageBlobKey = uploader.uploadImage(imageFile);
                param.setImageBlobKey(imageBlobKey);
                // 2. insert bean to cloud
                PoiOverviewBean result = poiServices.insertPoi(param);
                // 3. insert the result into the local database
                PoiTableUpdater updater = new PoiTableUpdater(fContext);
                updater.insertPoiOverview(result);
                return result;
            }
            finally {
                if (r != null) {
                    r.close();
                }
            }
        }
    }
}
