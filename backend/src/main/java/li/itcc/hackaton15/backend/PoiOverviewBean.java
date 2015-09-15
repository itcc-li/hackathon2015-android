package li.itcc.hackaton15.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiOverviewBean {
    private long poiId;
    private double longitude;
    private double latitude;
    private String poiName;
    private String poiDescription;
    private float rating;
    private byte[] thumbnail_135;

    public long getPoiId() {
        return poiId;
    }

    public void setPoiId(long poiId) {
        this.poiId = poiId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getPoiDescription() {
        return poiDescription;
    }

    public void setPoiDescription(String poiDescription) {
        this.poiDescription = poiDescription;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public byte[] getThumbnail_135() {
        return thumbnail_135;
    }

    public void setThumbnail_135(byte[] thumbnail_135) {
        this.thumbnail_135 = thumbnail_135;
    }



}