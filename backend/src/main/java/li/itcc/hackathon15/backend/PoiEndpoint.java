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
        list[0] = createBean("Städtle Vaduz", 47.140937, 9.520890, 3.5f, 1L);
        list[1] = createBean("Kirchile Malbun", 47.102931, 9.610188, 4.2f, 2L);
        list[2] = createBean("Guatabärg Burg", 47.065425, 9.500695, 4.8f, 3L);
        response.setList(list);
        return response;
    }

    /**
     * Inserts a new poi into the database
     * @param newPoi
     * @return
     */
    @ApiMethod(name = "insertPoi")
    public PoiCreateResultBean insertPoi(PoiCreateBean newPoi) {
        PoiCreateResultBean result = new PoiCreateResultBean();
        result.setPoiId(5L);
        return result;
    }

    //// private helpers

    private PoiOverviewBean createBean(String poiName, double latitude, double longitude, float rating, long poiId) {
        PoiOverviewBean result = new PoiOverviewBean();
        result.setPoiName(poiName);
        result.setLatitude(latitude);
        result.setLongitude(longitude);
        result.setRating(rating);
        result.setPoiId(poiId);
        return result;
    }

}
