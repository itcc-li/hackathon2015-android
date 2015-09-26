package li.itcc.hackathon15.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import li.itcc.hackathon15.util.BackendUtils;

/**
 * Created by Arthur on 18.09.2015.
 */
public class UploadCallback extends HttpServlet {
    private static final Logger log = Logger.getLogger(UploadCallback.class.getName());

    private BlobstoreService fBlobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setCharacterEncoding("utf-8");
        Map<String, List<BlobKey>> blobs = fBlobstoreService.getUploads(req);
        List<BlobKey> blobKeys = blobs.get("image");
        if (blobKeys == null || blobKeys.size() != 1) {
            setBadRequest(res, null, "Exactly one picture with name \"image\" is required");
            return;
        }
        BlobKey blobKey = blobKeys.get(0);
        // verify image
        BackendUtils.getVerifiedImage(blobKey);
        // return the key
        String key = blobKey.getKeyString();
        res.setStatus(HttpServletResponse.SC_OK);
        String contentType = "text/plain; charset=" + res.getCharacterEncoding();
        res.setContentType(contentType);
        res.getWriter().print(key);
    }


    private void setBadRequest(HttpServletResponse res, BlobKey blobKey, String text) throws IOException {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        log.warning("Bad image upload request: " + text);
        if (blobKey != null) {
            fBlobstoreService.delete(blobKey);
        }
    }
}

