/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package li.itcc.hackathon15.backend;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.images.Transform;
import com.googlecode.objectify.Key;

import li.itcc.hackathon15.PoiConstants;
import li.itcc.hackathon15.entities.PoiEntity;
import li.itcc.hackathon15.servlets.UploadCallback;
import li.itcc.hackathon15.util.BackendUtils;

import static li.itcc.hackathon15.datastore.OfyService.ofy;

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
    private static final Logger log = Logger.getLogger(UploadCallback.class.getName());

    /**
     * Returns a list of poi for the given latitude and logitude.
     *
     * @param latitude
     * @param longitude
     * @param maxCount
     * @return The list
     */
    @ApiMethod(name = "getPoiOverviewList")
    public PoiOverviewListBean getPoiOverviewList(@Named("latitude") double latitude, @Named("longitude") double longitude, @Named("maxCount") int maxCount) {
        if (maxCount <= 0 || maxCount > 200) {
            maxCount = 200;
        }
        QueryResultIterator<PoiEntity> result = ofy().load().type(PoiEntity.class).limit(maxCount).iterator();
        ArrayList<PoiOverviewBean> beans = new ArrayList<>();
        while (result.hasNext()) {
            PoiEntity entity = result.next();
            beans.add(createBean(entity));
        }
        PoiOverviewListBean response = new PoiOverviewListBean();
        PoiOverviewBean[] list = beans.toArray(new PoiOverviewBean[beans.size()]);
        response.setList(list);
        return response;
    }

    @ApiMethod(name = "getPoiDetails")
    public PoiDetailBean getPoiDetails(@Named("poiuuid") String uuid) {
        if (uuid == null) {
            throw new NullPointerException();
        }
        // validate uuid
        UUID.fromString(uuid);
        Key<PoiEntity> poiKey = Key.create(PoiEntity.class, uuid);
        PoiEntity entity = ofy().load().key(poiKey).now();
        if (entity == null) {
            return null;
        }
        PoiOverviewBean overview = createBean(entity);
        PoiDetailBean result = new PoiDetailBean();
        result.setOverview(overview);
        result.setDescription(entity.getDescription());
        String imageBlobKey = entity.getImageBlobKey();
        if (imageBlobKey != null) {
            BlobKey blobKey = new BlobKey(imageBlobKey);
            // get blob size
            BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
            BlobInfo info = blobInfoFactory.loadBlobInfo(blobKey);
            result.setImageSize(info.getSize());
            // create image download url
            ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
            ImagesService imageService = ImagesServiceFactory.getImagesService();
            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            try {
                String url = imageService.getServingUrl(options);
                result.setImageUrl(url);
            }
            catch (Exception x) {
                log.warning("Could not load image url " + x.getClass().getName() + " " + x.getMessage());
            }
            result.setImageUpdateTime(entity.getImageUpdateTime());
        }
        return result;
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
     *
     * @param request
     * @return
     */
    @ApiMethod(name = "insertPoi")
    public PoiOverviewBean insertPoi(PoiCreateBean request) throws Exception {
        // 1. validation
        String uuid = request.getUuid();
        UUID.fromString(uuid);
        double latitude = request.getLatitude();
        if (latitude > 90.0 || latitude < -90.0) {
            throw new IllegalArgumentException("Bad latitude " + latitude);
        }
        double longitude = request.getLongitude();
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Bad longitude " + longitude);
        }
        Double exactLatitude = request.getExactLatitude();
        Double exactLongitude = request.getExactLongitude();
        if (exactLatitude == null ^ exactLongitude == null) {
            throw new IllegalArgumentException("Exact latitude must both be null or not null");
        }
        if (exactLatitude != null && exactLongitude != null) {
            double distance = getDisance(latitude, longitude, exactLatitude, exactLongitude);
            // give 1 meter tolerance
            if (distance > PoiConstants.FINE_LOCATION_MAX_RADIUS + 1) {
                throw new IllegalArgumentException("Exact location out of radius");
            }
        }
        String name = request.getPoiName();
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }
        int nameLen = name.length();
        if (nameLen < PoiConstants.POI_NAME_LENGTH_MIN || nameLen > PoiConstants.POI_NAME_LENGTH_MAX) {
            throw new IllegalArgumentException("Bad name len " + nameLen);
        }
        String description = request.getPoiDescription();
        if (description != null && description.length() > PoiConstants.POI_COMMENT_LENGTH_MAX) {
            throw new IllegalArgumentException("Description too long:" + description.length());
        }
        String imageBlobKey = request.getImageBlobKey();
        byte[] thumbNail = null;
        if (imageBlobKey != null) {
            // check image
            BlobKey blobKey = new BlobKey(imageBlobKey);
            Image originalImage = BackendUtils.getVerifiedImage(blobKey);
            // now, resize the image
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            Transform resize = ImagesServiceFactory.makeResize(108, 108);
            Image newImage = imagesService.applyTransform(resize, originalImage);
            thumbNail = newImage.getImageData();
        }
        // 2. store
        long now = System.currentTimeMillis();
        PoiEntity entity = new PoiEntity();
        entity.setUuid(uuid);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setExactLatitude(exactLatitude);
        entity.setExactLongitude(exactLongitude);
        entity.setName(name);
        entity.setDescription(description);
        entity.setImageBlobKey(imageBlobKey);
        entity.setThumbnail(thumbNail);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCommentUpdateTime(now);
        entity.setImageUpdateTime(now);
        ofy().save().entity(entity).now();
        // 3. create result
        PoiOverviewBean result = createBean(entity);
        return result;
    }

    //// private helpers

    private PoiOverviewBean createBean(PoiEntity entity) {
        PoiOverviewBean result = new PoiOverviewBean();
        result.setUuid(entity.getUuid());
        Double exactLatitude = entity.getExactLatitude();
        Double exactLongitude = entity.getExactLongitude();
        if (exactLatitude != null && exactLongitude != null) {
            result.setLatitude(exactLatitude);
            result.setLongitude(exactLongitude);
        }
        else {
            result.setLatitude(entity.getLatitude());
            result.setLongitude(entity.getLongitude());
        }
        result.setName(entity.getName());
        result.setShortDescription(entity.getShortDescription());
        result.setRating(entity.getRating());
        result.setThumbnailBase64(entity.getThumbnailBase64());
        return result;
    }


    public static double getDisance(double lat1, double lng1, double lat2, double lng2) {
        // see https://en.wikipedia.org/wiki/Great-circle_distance
        double lat1Rad = Math.toRadians(lat1);
        double lng1Rad = Math.toRadians(lng1);
        double lat2Rad = Math.toRadians(lat2);
        double lng2Rad = Math.toRadians(lng2);
        double sec1 = Math.sin(lat1Rad) * Math.sin(lat2Rad);
        double sec2 = Math.cos(lat1Rad) * Math.cos(lat2Rad);
        double dLngRad = Math.abs(lng1Rad - lng2Rad);
        double centralAngle = Math.acos(sec1 + sec2 * Math.cos(dLngRad));
        double distanceMeters = centralAngle * PoiConstants.EARTH_RADIUS_IN_METER;
        return distanceMeters;
    }
}
