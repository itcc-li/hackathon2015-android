package li.itcc.hackathon15.util;

import java.io.IOException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.repackaged.com.google.common.io.ByteStreams;

/**
 * Created by Arthur on 19.09.2015.
 */
public class BackendUtils {

    public static Image getImage(BlobKey blobKey) throws IOException {
        byte[] data = getData(blobKey);
        Image image = ImagesServiceFactory.makeImage(data);
        return image;
    }

    public static byte[] getData(BlobKey blobKey) throws IOException {
        return ByteStreams.toByteArray(new BlobstoreInputStream(blobKey));
    }

    public static Image getVerifiedImage(BlobKey blobKey) throws IOException {
        // verify if the image is o.k.
        Image theImage = BackendUtils.getImage(blobKey);
        Image.Format format = theImage.getFormat();
        if (format != Image.Format.JPEG) {
            throwBadRequest(blobKey, "Image type must be jpg");
        }
        int requestedImageSize = 1080;
        if (theImage.getWidth() != requestedImageSize || theImage.getHeight() != requestedImageSize) {
            throwBadRequest(blobKey, "Image size must be " + requestedImageSize);
        }
        return theImage;
    }

    private static void throwBadRequest(BlobKey blobKey, String s) throws IOException {
        BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
        throw new IllegalArgumentException(s);
    }

}
