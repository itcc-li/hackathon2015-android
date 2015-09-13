package li.itcc.hackathon15.services;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiFullDetailBean {
    private String poiName;
    private float rating;
    private byte[] image;

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }



}
