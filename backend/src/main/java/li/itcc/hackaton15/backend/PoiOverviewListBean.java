package li.itcc.hackaton15.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiOverviewListBean {
    public PoiOverviewBean[] getList() {
        return list;
    }

    public void setList(PoiOverviewBean[] list) {
        this.list = list;
    }

    private PoiOverviewBean[] list;

}