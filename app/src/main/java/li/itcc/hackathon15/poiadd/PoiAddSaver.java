package li.itcc.hackathon15.poiadd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;

import javax.net.ssl.HttpsURLConnection;

import li.itcc.hackathon15.PoiConstants;
import li.itcc.hackathon15.ReleaseConfig;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateResultBean;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.util.StreamUtil;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddSaver {
    private final Context fContext;
    private final PoiAddSaveProgressListener fListener;
    private File fLocalImageFile;

    public void setLocalImageFile(File localImageFile) {
        fLocalImageFile = localImageFile;
    }

    public interface PoiAddSaveProgressListener {

        void onProgress(int percent);

        void onDetailSaved(PoiCreateResultBean newBean, Throwable th);
    }

    public PoiAddSaver(Context context, PoiAddSaveProgressListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void save(PoiCreateBean bean) {
        new SaveTask().execute(bean);
    }

    private class SaveTask extends AsyncTask<PoiCreateBean, Integer, PoiCreateResultBean> {
        private Throwable fException;

        public SaveTask() {
        }

        @Override
        protected PoiCreateResultBean doInBackground(PoiCreateBean... params) {
            try {
                RandomAccessFile r = null;
                try {
                    PoiCreateBean param = params[0];
                    PoiServices poiServices = new PoiServices(fContext, PoiConstants.URL);
                    // 1. upload the image file in this thread
                    File imageFile = fLocalImageFile;
                    String imageId = uploadBlob(poiServices, imageFile);
                    param.setImageBlobUrl(imageId);
                    // 2. insert bean to cloud
                    PoiCreateResultBean result = poiServices.insertPoi(param);
                    // 3. insert the result into the local database
                    PoiTableUpdater updater = new PoiTableUpdater(fContext);
                    updater.insertPoiOverview(param, result);
                    return result;
                }
                finally {
                    if (r != null) {
                        r.close();
                    }
                }
            }
            catch (Throwable th) {
                fException = th;
            }
            return null;
        }


        private static final String BOUNDARY = "ayfQuegbhrmjYtdLwPdsfitergKyhbwjAM25z9AJuGSx7WG9dnD";
        private static final int JUNK_SIZE = 10000;

        private String uploadBlob(PoiServices services, File imageFile) throws Exception {
            InputStream inputStream;
            long fileSize;
            if (!imageFile.exists() || !imageFile.canRead()) {
                // debug version
                byte[] tmp = new byte[50];
                for (int i=0; i<tmp.length; i++) {
                    tmp[i] = (byte)('A' + i);
                }
                inputStream = new ByteArrayInputStream(tmp);
                fileSize = tmp.length;
            }
            else {
                inputStream = new FileInputStream(imageFile);
                fileSize = imageFile.length();
            }
            if (fileSize == 0) {
                return null;
            }
            String postURL = services.getImageUploadUrl().getUrl();
            // patch the url for local testing
            postURL = ReleaseConfig.dnsHack(postURL);
            URL url = new URL(postURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            // stream the upload in junks so that we don't load the whole file in memory
            //connection.setChunkedStreamingMode(JUNK_SIZE);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("X-AppEngine-BlobUpload", "true");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            //OutputStream outBin = connection.getOutputStream();
            ByteArrayOutputStream outBin = new ByteArrayOutputStream();
            //attach post objects
            DataOutputStream outputStream = new DataOutputStream(outBin);
            String formElementName = "image";
            String fileName = "image.jpg";
            //build the request body
            outputStream.writeBytes("--" + BOUNDARY + "\r\n" +
                  "Content-Disposition: form-data; name=\"" + formElementName + "\"; filename=\"" + fileName + "\"\r\n" +
                  // "Content-Length: 50\r\n" +
                  // "Content-Type: image/jpeg\r\n" +
            "\r\n");
            long totalUploadSize = fileSize;
            byte[] buffer = new byte[JUNK_SIZE];
            long totalBytesWritten = 0;
            int bytesRead = 0;
            // read directly from file also in junks
            while ((bytesRead = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesWritten += bytesRead;
                int percentage = (int)(totalBytesWritten * 100L / totalUploadSize);
                onProgressUpdate(percentage);
            }
            // write final boundary
            outputStream.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
            outputStream.flush();
            outputStream.close();

            byte[] body = outBin.toByteArray();
            String bodyString = new String(body);

            OutputStream bodyOut = connection.getOutputStream();
            bodyOut.write(body);
            bodyOut.flush();
            bodyOut.close();

            int responseCode = connection.getResponseCode();
            String response = "";
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }
            return response;
        }

        private String uploadBlob_(PoiServices services, File imageFile) throws Exception {
            String requestURL = services.getImageUploadUrl().getUrl();
            // patch the url for local testing
            requestURL = ReleaseConfig.dnsHack(requestURL);
            URL url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-AppEngine-BlobUpload", "true");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            InputStream in;
            if (imageFile.exists() && imageFile.canRead()) {
                in = new FileInputStream(imageFile);
            }
            else {
                byte[] data = new byte[1000];
                in = new ByteArrayInputStream(data);
            }
            long size = StreamUtil.pumpAllAndClose(in, out);
            int responseCode = connection.getResponseCode();
            String response = "";
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }
            else {
                response = "";
            }
            return response;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (fListener != null) {
                fListener.onProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(PoiCreateResultBean newBean) {
            super.onPostExecute(newBean);
            if (fListener != null) {
                fListener.onDetailSaved(newBean, fException);
            }
        }
    }
}
