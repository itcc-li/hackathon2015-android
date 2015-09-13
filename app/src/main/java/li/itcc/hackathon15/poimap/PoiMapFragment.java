package li.itcc.hackathon15.poimap;


import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.LoaderManager;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.TitleHolder;
import li.itcc.hackathon15.database.DatabaseContract;
import li.itcc.hackathon15.gps.GPSDeliverer;
import li.itcc.hackathon15.gps.GPSLocationListener;
import li.itcc.hackathon15.poiadd.PoiAddOnClickListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiMapFragment extends SupportMapFragment implements GPSLocationListener, LoaderManager.LoaderCallbacks<Cursor>,PoiAddOnClickListener.LocationProvider {
    private GoogleMap fMap;
    private int fFunction;
    private GPSDeliverer fGpsDeliverer;
    private int fPointCounter;
    private Marker fMarker;
    private ArrayList<Marker> fMarkers = new ArrayList<Marker>();
    private View fCreateButton;
    private Location fLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        // trick: we have to add a floating button so we add an extra layer
        container.removeView(v);
        fMap = getMap();
        View rootView = inflater.inflate(R.layout.poi_map_fragment, container, false);
        FrameLayout frame = (FrameLayout)rootView.findViewById(R.id.frame_layout);
        fCreateButton = rootView.findViewById(R.id.viw_add_button);
        fCreateButton.setOnClickListener(new PoiAddOnClickListener(getActivity(), this));
        // we have to wait for the current location.
        fCreateButton.setEnabled(false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.removeView(v);
        frame.addView(v, params);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if (item.getItemId() == R.id.action_example) {
        //    exampleAction();
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TitleHolder) {
            ((TitleHolder)context).setTitleId(R.string.title_overview_map);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        fGpsDeliverer = new GPSDeliverer(getActivity(), 0L);
        fGpsDeliverer.setListener(this);
        fGpsDeliverer.setAutoReset(false);
        fGpsDeliverer.startDelivery();
    }

    @Override
    public void onLocation(Location location) {
        if (location == null) {
            return;
        }
        fLocation = location;
        fCreateButton.setEnabled(true);
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        String title = Integer.toString(fPointCounter++);
        fMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.5f));
        if (fMarker == null) {
            fMarker = fMap.addMarker(new MarkerOptions()
                    .title(title)
                    .position(loc));
        }
        else {
            fMarker.setPosition(loc);
            fMarker.setTitle(title);
        }
        if (fGpsDeliverer != null) {
            fGpsDeliverer.stopDelivery();
            fGpsDeliverer = null;
        }
    }

    @Override
    public void onLocationSensorSearching() {

    }

    @Override
    public void onLocationSensorEnabled() {

    }

    @Override
    public void onLocationSensorDisabled() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = null;
        String where = null;
        String[] whereArgs = null;
        String sortOrder = null;
        Uri queryUri = DatabaseContract.Pois.CONTENT_URI;
        CursorLoader loader = new CursorLoader(getActivity(), queryUri, projection, where, whereArgs, sortOrder);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        clearAllMarkers();
        if (data == null) {
            return;
        }
        int longitudeCol = data.getColumnIndex(DatabaseContract.Pois.POI_LONGITUDE);
        int latitudeCol = data.getColumnIndex(DatabaseContract.Pois.POI_LATITUDE);
        int nameCol = data.getColumnIndex(DatabaseContract.Pois.POI_NAME);
        if (data.moveToFirst()) {
            do {
                double longitude = data.getDouble(longitudeCol);
                double latitude = data.getDouble(latitudeCol);
                String name = data.getString(nameCol);
                LatLng loc = new LatLng(latitude, longitude);
                Marker marker = fMap.addMarker(new MarkerOptions()
                        .title(name)
                        .position(loc));
                fMarkers.add(marker);
            } while (data.moveToNext());

        }
    }

    private void clearAllMarkers() {
        for (Marker marker: fMarkers) {
            marker.remove();
        }
        fMarkers.clear();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public Location getLocation() {
        return fLocation;
    }
}
