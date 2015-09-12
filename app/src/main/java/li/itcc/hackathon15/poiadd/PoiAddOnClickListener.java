package li.itcc.hackathon15.poiadd;

import android.app.Activity;
import android.location.Location;
import android.view.View;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddOnClickListener implements View.OnClickListener {

    private final Activity fParent;
    private final LocationProvider fProvider;


    public interface LocationProvider {
        Location getLocation();
    }

    public PoiAddOnClickListener(Activity parent, LocationProvider provider) {
        fParent = parent;
        fProvider = provider;
    }

    @Override
    public void onClick(View v) {
        Location location = fProvider.getLocation();
        if (location == null) {
            return;
        }
        PoiAddActivity.start(fParent, location);
    }
}
