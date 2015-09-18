package li.itcc.hackathon15.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiOverviewListBean {
    private PoiOverviewBean[] list;


    public PoiOverviewBean[] getList() {
        return list;
    }

    public void setList(PoiOverviewBean[] list) {
        this.list = list;
    }


}