package li.itcc.hackathon15.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import li.itcc.hackathon15.util.Base64;

/**
 * Created by Arthur on 20.09.2015.
 */
@Entity
public class PoiEntity {
    @Id
    String uuid;
    double longitude;
    double latitude;
    Double exactLongitude;
    Double exactLatitude;
    String name;
    String description;
    String imageBlobKey;
    float ratingSum;
    int ratingCount;
    float rating;
    byte[] thumbnail;
    long createTime;
    long updateTime;
    long imageUpdateTime;
    long commentUpdateTime;

    // don't remove this explicit no-arg constructor
    public PoiEntity() {
    }

    // special logic

    public String getThumbnailBase64() {
        if (thumbnail == null) {
            return null;
        }
        String base64 = Base64.encodeToString(thumbnail, Base64.DEFAULT);
        return base64;
    }

    public String getShortDescription() {
        return shorten(getDescription(), 80);
    }

    private String shorten(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        text = text.substring(0, maxLen-3) + "...";
        return text;
    }

    public void addRating(float rating) {
        ratingSum += rating;
        ratingCount++;
        rating = ratingSum / ratingCount;
    }

    // getters and setters


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageBlobKey() {
        return imageBlobKey;
    }

    public void setImageBlobKey(String imageBlobKey) {
        this.imageBlobKey = imageBlobKey;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public float getRating() {
        return rating;
    }

    public float getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(float ratingSum) {
        this.ratingSum = ratingSum;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCommentUpdateTime() {
        return commentUpdateTime;
    }

    public void setCommentUpdateTime(long commentUpdateTime) {
        this.commentUpdateTime = commentUpdateTime;
    }


    public long getImageUpdateTime() {
        return imageUpdateTime;
    }

    public void setImageUpdateTime(long imageUpdateTime) {
        this.imageUpdateTime = imageUpdateTime;
    }

    public Double getExactLongitude() {
        return exactLongitude;
    }

    public void setExactLongitude(Double exactLongitude) {
        this.exactLongitude = exactLongitude;
    }

    public Double getExactLatitude() {
        return exactLatitude;
    }

    public void setExactLatitude(Double exactLatitude) {
        this.exactLatitude = exactLatitude;
    }
}
