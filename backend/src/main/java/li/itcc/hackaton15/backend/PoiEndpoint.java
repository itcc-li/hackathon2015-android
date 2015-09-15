/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package li.itcc.hackaton15.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "poiApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.hackaton15.itcc.li",
                ownerName = "backend.hackaton15.itcc.li",
                packagePath = ""
        )
)
public class PoiEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "getPoiOverviewList")
    public PoiOverviewListBean getPoiOverviewList(PoiOverviewQueryBean query) {
        PoiOverviewListBean response = new PoiOverviewListBean();
        PoiOverviewBean[] list = new PoiOverviewBean[3];
        list[0] = createBean("Städtle Vaduz", 47.140937, 9.520890, 3.5f, 1L);
        list[1] = createBean("Kirchile Malbun", 47.102931, 9.610188, 4.2f, 2L);
        list[2] = createBean("Guatabärg Burg", 47.065425, 9.500695, 4.8f, 3L);
        response.setList(list);
        return response;
    }

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
