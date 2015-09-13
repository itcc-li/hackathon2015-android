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
import li.itcc.hackathon15.ToastResultListener;
import li.itcc.hackathon15.poiadd.PoiAddSaver;
import li.itcc.hackathon15.services.PoiFullDetailBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailActivity extends AppCompatActivity implements PoiDetailLoader.PoiDetailLoadDoneListener {
    private static final String KEY_ID = "KEY_ID";
    private TextView fName;
    private EditText fComment;
    private RatingBar fRating;
    private Location fLocation;
    private ImageView fImage;
    private ProgressBar fProgressBar;

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
        fRating = (RatingBar)findViewById(R.id.rbr_rating);
        fRating.setIsIndicator(true);
        fImage = (ImageView)findViewById(R.id.img_image);
        fProgressBar = (ProgressBar)findViewById(R.id.prb_progress);
        fProgressBar.setIndeterminate(true);
        fProgressBar.setVisibility(View.VISIBLE);
        long id = getIntent().getExtras().getLong(KEY_ID);
        new PoiDetailLoader(this, this).load(id);
    }


    @Override
    public void onPoiDetailsLoaded(PoiFullDetailBean data, Throwable th) {
        fProgressBar.setVisibility(View.GONE);
        if (th != null) {
            new ToastResultListener(this).onRefreshDone(th);
            return;
        }
        // show the data
        byte[] image = data.getImage();
        if (image != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            fImage.setImageBitmap(bmp);
        }
        fRating.setRating(data.getRating());
        fName.setText(data.getPoiName());

    }
}
