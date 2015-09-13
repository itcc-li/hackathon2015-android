package li.itcc.hackathon15.poilist;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.TitleHolder;
import li.itcc.hackathon15.database.DatabaseContract;
import li.itcc.hackathon15.gps.GPSDeliverer;
import li.itcc.hackathon15.gps.GPSLocationListener;
import li.itcc.hackathon15.poiadd.PoiAddOnClickListener;


/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,PoiAddOnClickListener.LocationProvider, GPSLocationListener {
    private BookCursorAdapter fDataAdapter;
    private ListView fListView;
    private TextView fEmptyText;
    private View fCreateButton;
    private Location fLocation;
    private GPSDeliverer fGpsDeliverer;

    public PoiListFragment() {
    }

    //@Override
    //public void onAttach(Activity activity) {
    //    super.onAttach(activity);
    //    setHasOptionsMenu(true);
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        View rootView = inflater.inflate(R.layout.poi_list_fragment, container, false);
        fListView = (ListView)rootView.findViewById(android.R.id.list);
        fEmptyText = (TextView)rootView.findViewById(android.R.id.empty);
        fCreateButton = rootView.findViewById(R.id.viw_add_button);
        fCreateButton.setOnClickListener(new PoiAddOnClickListener(getActivity(), this));
        fCreateButton.setVisibility(View.INVISIBLE);
        fDataAdapter = new BookCursorAdapter(activity);
        fListView.setAdapter(fDataAdapter);
        fListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(position, id);
            }
        });
        updateTableVisibility();
        return rootView;
    }


    private void onListItemClick(int position, long id) {
        //Intent intent = BookDetailActivity.createStartIntent(getActivity(), id);
        //startActivity(intent);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.add_poi, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if (item.getItemId() == R.id.action_add_poi) {
        //    Intent intent = AddBookActivity.createStartIntent(getActivity());
        //    startActivity(intent);
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    private void updateTableVisibility() {
        if (fDataAdapter.getCount() == 0) {
            fEmptyText.setVisibility(View.VISIBLE);
            fListView.setVisibility(View.GONE);
        }
        else {
            fEmptyText.setVisibility(View.GONE);
            fListView.setVisibility(View.VISIBLE);
        }
    }

    //// loader callbacks

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
        this.fDataAdapter.swapCursor(data);
        updateTableVisibility();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TitleHolder) {
            ((TitleHolder)context).setTitleId(R.string.title_poi_list);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.fDataAdapter.swapCursor(null);
    }

    @Override
    public Location getLocation() {
        return fLocation;
    }

    @Override
    public void onLocation(Location location) {
        if (location == null) {
            return;
        }
        fLocation = location;
        fCreateButton.setVisibility(View.VISIBLE);
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

    private static class BookCursorAdapter extends SimpleCursorAdapter {

        public BookCursorAdapter(Context context) {
            super(context, R.layout.poi_list_row, null, new String[]{DatabaseContract.Pois.POI_NAME}, new int[]{R.id.txv_poi_name}, 0);
        }
    }
}

