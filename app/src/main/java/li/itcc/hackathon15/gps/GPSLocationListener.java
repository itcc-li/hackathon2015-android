
package li.itcc.hackathon15.gps;

import android.location.Location;

public interface GPSLocationListener {

    public void onLocation(Location location);
    
    public void onLocationSensorSearching();

    public void onLocationSensorEnabled();

    public void onLocationSensorDisabled();

}
