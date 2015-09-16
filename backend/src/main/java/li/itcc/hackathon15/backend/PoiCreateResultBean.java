package li.itcc.hackathon15.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiCreateResultBean {
    private Long poiId;

    /**
     * The id of the created poi.
     * @return
     */
    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

}