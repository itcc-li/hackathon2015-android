package li.itcc.hackathon15.poiadd;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import li.itcc.hackathon15.ReleaseConfig;
import li.itcc.hackathon15.services.PoiServices;

/**
 * Created by Arthur on 19.09.2015.
 */
public class ImageUploader {

    private static final String BOUNDARY = "ayfQuegbhrmjYtdLwPdsfitergKyhbwjAM25z9AJuGSx7WG9dnD";
    private static final int JUNK_SIZE = 10000;
    private final ProgressListener fListener;
    private final PoiServices fServices;

    public ImageUploader(PoiServices services, ProgressListener listener) {
        fListener = listener;
        fServices = services;
    }

    public String uploadImage(File imageFile) throws Exception {
        if (!imageFile.exists() || !imageFile.canRead()) {
            return null;
        }
        long fileSize = imageFile.length();
        if (fileSize == 0) {
            return null;
        }
        InputStream inputStream = new FileInputStream(imageFile);;
        // stream is ready, get url to upload
        String postURL = fServices.getImageUploadUrl().getUrl();
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
        // this does not work
        //connection.setChunkedStreamingMode(JUNK_SIZE);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("X-AppEngine-BlobUpload", "true");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        OutputStream outBin = connection.getOutputStream();
        //attach post objects
        DataOutputStream outputStream = new DataOutputStream(outBin);
        //build the request body
        outputStream.writeBytes("--" + BOUNDARY + "\r\n" +
                "Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n" +
                "Content-Type: image/jpeg\r\n" +
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
            fListener.onProgressChanged(percentage);
        }
        // write final boundary
        outputStream.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
        outputStream.flush();
        outputStream.close();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            return response;
        }
        throw new IOException("Image upload failed with code " + responseCode);
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuffer response = new StringBuffer();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "utf-8");
        int ch;
        while ((ch = reader.read()) != -1) {
            response.append((char)ch);
        }
        return response.toString();
    }

}
