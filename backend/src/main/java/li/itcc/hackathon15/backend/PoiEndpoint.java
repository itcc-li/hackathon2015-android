/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package li.itcc.hackathon15.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.repackaged.com.google.common.util.Base64;

import li.itcc.hackathon15.util.BackendUtils;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "poiApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.hackathon15.itcc.li",
                ownerName = "backend.hackathon15.itcc.li",
                packagePath = ""
        )
)
public class PoiEndpoint {

    /**
     * Returns a list of poi for the given latitude and logitude.
     * @param latitude
     * @param longitude
     * @param maxCount
     * @return The list
     */
    @ApiMethod(name = "getPoiOverviewList")
    public PoiOverviewListBean getPoiOverviewList(@Named("latitude") double latitude, @Named("longitude") double longitude, @Named("maxCount") int maxCount) {
        PoiOverviewListBean response = new PoiOverviewListBean();
        PoiOverviewBean[] list = new PoiOverviewBean[3];
        list[0] = createBean("Vaduz Städtle", 47.140937, 9.520890, 3.5f, 1L, "Ein lässiges Städtle, mitten in Liechtenstein");
        list[1] = createBean("Kirchile Malbun", 47.102931, 9.610188, 4.2f, 2L, "Kleines Kirchile am Rande von Malbun");
        list[2] = createBean("Guatabärg Burg", 47.065425, 9.500695, 4.8f, 3L, "Mitten in Balzers auf einem Hügel gelegene Burg");
        response.setList(list);
        return response;
    }

    @ApiMethod(name = "getImageUploadUrl")
    public ImageUploadUrlBean getImageUploadUrl() {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        // Note: /uploadcallback is the callback that the blobstore will call
        // as soon as the upload is done. We have our own servlet there
        String blobUploadUrl = blobstoreService.createUploadUrl("/uploadcallback");
        ImageUploadUrlBean bean = new ImageUploadUrlBean();
        bean.setUrl(blobUploadUrl);
        return bean;
    }

    /**
     * Inserts a new poi into the database
     * @param newPoi
     * @return
     */
    @ApiMethod(name = "insertPoi")
    public PoiOverviewBean insertPoi(PoiCreateBean newPoi) throws Exception {
        PoiOverviewBean result = new PoiOverviewBean();
        // check all values
        String imageBlobKey = newPoi.getImageBlobKey();
        if (imageBlobKey != null) {
            // check image
            BlobKey blobKey = new BlobKey(imageBlobKey);
            Image originalImage = BackendUtils.getVerifiedImage(blobKey);
            // now, resize the image
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            Transform resize = ImagesServiceFactory.makeResize(108, 108);
            Image newImage = imagesService.applyTransform(resize, originalImage);
            byte[] newImageData = newImage.getImageData();
            // temp hack
            String base64 = Base64.encode(newImageData, 0, newImageData.length, Base64.getAlphabet(), true);
            result.setThumbnailBase64(base64);
        }
        result.setLatitude(newPoi.getLatitude());
        result.setLongitude(newPoi.getLongitude());
        result.setPoiName(newPoi.getPoiName());
        result.setShortPoiDescription(shorten(newPoi.getPoiDescription(), 80));
        result.setRating(0.0f);
        result.setPoiId(5L);
        return result;
    }

    private String shorten(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        text = text.substring(0, maxLen-3) + "...";
        return text;
    }


    //// private helpers

    private PoiOverviewBean createBean(String poiName, double latitude, double longitude, float rating, long poiId, String shortDesc) {
        PoiOverviewBean result = new PoiOverviewBean();
        result.setPoiName(poiName);
        result.setShortPoiDescription(shortDesc);
        result.setLatitude(latitude);
        result.setLongitude(longitude);
        result.setRating(rating);
        result.setPoiId(poiId);
        return result;
    }

}
