package li.itcc.hackathon15.poidetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.backend.poiApi.model.PoiDetailBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackathon15.util.ExceptionHandler;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailActivity extends AppCompatActivity implements PoiDetailLoader.PoiDetailLoaderListener {
    private static final String KEY_ID = "KEY_ID";
    private TextView fName;
    private EditText fComment;
    private RatingBar fRating;
    private Location fLocation;
    private ImageView fImage;
    private ProgressBar fProgressBar;
    private TextView fDescription;

    public static void start(Activity parent, long poiId) {
        Intent i = new Intent(parent, PoiDetailActivity.class);
        i.putExtra(KEY_ID, poiId);
        parent.startActivityForResult(i, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail_activity);
        fName = (TextView)findViewById(R.id.txv_name);
        fDescription = (TextView)findViewById(R.id.txv_description);
        fRating = (RatingBar)findViewById(R.id.rbr_rating);
        fRating.setIsIndicator(true);
        fImage = (ImageView)findViewById(R.id.img_image);
        fProgressBar = (ProgressBar)findViewById(R.id.prb_progress);
        fProgressBar.setMax(100);
        fProgressBar.setVisibility(View.VISIBLE);
        long id = getIntent().getExtras().getLong(KEY_ID);
        new PoiDetailLoader(this, this).load(id);
    }

    @Override
    public void onTaskCompleted(PoiDetailBean data) {
        fProgressBar.setVisibility(View.GONE);
        if (data.getImageUrl() != null) {
            // the image has been downloaded
            ImageStore store = new ImageStore(this);
            ImageStore.Key key = store.createKey(data.getOverview().getPoiId(), data.getImageUpdateTime());
            Bitmap bmp = BitmapFactory.decodeFile(store.getImageFilePath(key));
            fImage.setImageBitmap(bmp);
        }
        else {
            fImage.setImageDrawable(null);
        }
        PoiOverviewBean overviewData = data.getOverview();
        fRating.setRating(overviewData.getRating());
        fName.setText(overviewData.getPoiName());
        fDescription.setText(data.getDescription());
    }

    @Override
    public void onTaskProgress(int percentage) {
        fProgressBar.setProgress(percentage);
    }

    @Override
    public void onTaskAborted(Throwable th) {
        fProgressBar.setVisibility(View.GONE);
        new ExceptionHandler(this).onTaskAborted(th);
    }


}
