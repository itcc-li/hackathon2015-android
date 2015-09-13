package li.itcc.hackathon15.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

public class GPSDeliverer implements LocationListener {
    private GPSLocationListener fListener;
    private Context fContext;
    private LocationManager fLocManager;
    private long fSampleDeltaTimeMS;
    private long fNextSampleTimeMS = 0L;
    private boolean fHeartbeatReceived;
    private boolean fAutoReset = true;
    private double fLongSum = 0.0;
    private double fLatSum = 0.0;
    private int fPointCount = 0;

    public GPSDeliverer(Context context, long delay) {
        fContext = context;
        fSampleDeltaTimeMS = delay;
    }

    public void setListener(GPSLocationListener listener) {
        fListener = listener;
    }

    public void setAutoReset(boolean autoReset) {
        fAutoReset = autoReset;
    }

    public void startDelivery() {
        if (fListener == null) {
            throw new NullPointerException();
        }
        fHeartbeatReceived = false;
        fLocManager = (LocationManager) fContext
                .getSystemService(Context.LOCATION_SERVICE);
        try {
            fLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        catch(SecurityException x) {
        }
        Handler mainHandler = new Handler(fContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                checkHeartBeat();
            }
        };
        mainHandler.postDelayed(myRunnable, 1000);
    }

    protected void checkHeartBeat() {
        if (fHeartbeatReceived) {
            return;
        }
        if (fListener != null) {
            fListener.onLocationSensorSearching();
        }
    }

    public void stopDelivery() {
        if (fListener != null) {
            try {
                fLocManager.removeUpdates(this);
            }
            catch (SecurityException x) {
            }
            fListener = null;
        }
    }

    public boolean isRunning() {
        return fListener != null;
    }

    @Override
    public void onLocationChanged(Location location) {
        fHeartbeatReceived = true;
        if (fListener == null) {
            return;
        }
        if (fSampleDeltaTimeMS == 0) {
            fListener.onLocation(location);
        }
        location.getAccuracy();
        fLongSum += location.getLongitude();
        fLatSum += location.getLatitude();
        fPointCount++;
        long now = System.currentTimeMillis();
        if (fNextSampleTimeMS == 0 || now > fNextSampleTimeMS) {
            fNextSampleTimeMS = now + fSampleDeltaTimeMS;
            Location result = calculateAvgLocation();
            if (fAutoReset) {
                reset();
            }
            if (fListener != null) {
                fListener.onLocation(result);
            }
        }
    }

    public void reset() {
        fLongSum = 0.0;
        fLatSum = 0.0;
        fPointCount = 0;
    }

    private Location calculateAvgLocation() {
        if (fPointCount == 0) {
            return null;
        }
        double avgLat = fLatSum / fPointCount;
        double avgLong = fLongSum / fPointCount;
        Location location = new Location("AVG");
        location.setLatitude(avgLat);
        location.setLongitude(avgLong);
        return location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        fHeartbeatReceived = true;
        fListener.onLocationSensorEnabled();
    }

    @Override
    public void onProviderDisabled(String provider) {
        fHeartbeatReceived = true;
        fListener.onLocationSensorDisabled();
    }

}
