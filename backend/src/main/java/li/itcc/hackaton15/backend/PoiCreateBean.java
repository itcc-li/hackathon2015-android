package li.itcc.hackaton15.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiCreateBean {
    private double longitude;
    private double latitude;
    private String poiName;
    private String poiDescription;
    private String imageBlobUrl;

    public String getImageBlobUrl() {
        return imageBlobUrl;
    }

    public void setImageBlobUrl(String imageBlobUrl) {
        this.imageBlobUrl = imageBlobUrl;
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








}