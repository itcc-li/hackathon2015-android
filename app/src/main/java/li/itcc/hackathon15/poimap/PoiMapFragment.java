package li.itcc.hackathon15.poimap;


import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.TitleHolder;
import li.itcc.hackathon15.database.DatabaseContract;
import li.itcc.hackathon15.poiadd.PoiAddOnClickListener;
import li.itcc.hackathon15.poidetail.PoiDetailActivity;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String KEY_LOCATION_ZOOM_DONE = "KEY_LOCATION_ZOOM_DONE";
    private GoogleMap fMap;
    private HashMap<Marker, Long> fMarkers = new HashMap<>();
    private View fCreateButton;
    private Location fLocation;
    private boolean fLocationZoomDone = false;
    private GoogleApiClient fGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fLocationZoomDone = savedInstanceState.getBoolean(KEY_LOCATION_ZOOM_DONE);
        }
        View v = super.onCreateView(inflater, container, savedInstanceState);
        // trick: we have to add a floating button so we add an extra layer
        container.removeView(v);
        fMap = getMap();
        fMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                onClick(marker);
            }
        });
        fMap.getUiSettings().setMapToolbarEnabled(false);
        fMap.getUiSettings().setMyLocationButtonEnabled(false);
        //fMap.setMyLocationEnabled(true);
        View rootView = inflater.inflate(R.layout.poi_map_fragment, container, false);
        FrameLayout frame = (FrameLayout)rootView.findViewById(R.id.frame_layout);
        fCreateButton = rootView.findViewById(R.id.viw_add_button);
        fCreateButton.setOnClickListener(new PoiAddOnClickListener(getActivity()));
        //fCreateButton.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.removeView(v);
        frame.addView(v, params);
        return rootView;
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
        // start loading
        buildGoogleApiClient();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        fGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fGoogleApiClient.isConnected()) {
            fGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_LOCATION_ZOOM_DONE, fLocationZoomDone);
    }

    private synchronized void buildGoogleApiClient() {
        Context context = getContext();
        fGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean onClick(Marker marker) {
        Long id = fMarkers.get(marker);
        if (id != null) {
            PoiDetailActivity.start(getActivity(), id.longValue());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if (item.getItemId() == R.id.action_example) {
        //    exampleAction();
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    // google api client

    @Override
    public void onConnected(Bundle bundle) {
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(fGoogleApiClient);
        setLocation(lastKnownLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void setLocation(Location location) {
        if (location == null) {
            return;
        }
        if (!isAdded()) {
            return;
        }
        fLocation = location;
        if (!fLocationZoomDone) {
            // only zoom once
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            String title = getResources().getString(R.string.my_location);
            fMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 11.5f));
            fLocationZoomDone = true;
        }
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
        int descrCol = data.getColumnIndex(DatabaseContract.Pois.POI_SHORT_DESCRIPTION);
        int idCol = data.getColumnIndex(DatabaseContract.Pois.POI_ID);
        if (data.moveToFirst()) {
            do {
                double longitude = data.getDouble(longitudeCol);
                double latitude = data.getDouble(latitudeCol);
                String name = data.getString(nameCol);
                String shortDescr = data.getString(descrCol);
                long id = data.getLong(idCol);
                LatLng loc = new LatLng(latitude, longitude);
                Marker marker = fMap.addMarker(new MarkerOptions()
                        .title(name)
                        .snippet(shortDescr)
                        .position(loc).icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_pin)));
                fMarkers.put(marker, new Long(id));
            } while (data.moveToNext());

        }
    }

    private void clearAllMarkers() {
        for (Marker marker : fMarkers.keySet()) {
            marker.remove();
        }
        fMarkers.clear();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
