package li.itcc.hackathon15.datastore;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import li.itcc.hackathon15.entities.PoiEntity;

/**
 * Created by Arthur on 20.09.2015.
 */
public class OfyService {

    static {
        ObjectifyService.register(PoiEntity.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}