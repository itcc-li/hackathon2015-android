package li.itcc.hackathon15.poiadd;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import li.itcc.hackathon15.PoiDetailSaver;
import li.itcc.hackathon15.R;
import li.itcc.hackathon15.services.PoiDetailBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddActivity extends AppCompatActivity implements PoiDetailSaver.DetailSaveDoneListener {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private View fCancelButton;
    private View fSaveButton;
    private EditText fName;
    private EditText fComment;
    private RatingBar fRating;
    private Location fLocation;

    public static void start(Activity parent, Location location) {
        Intent i = new Intent(parent, PoiAddActivity.class);
        i.putExtra(KEY_LOCATION, location);
        parent.startActivityForResult(i, 0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_poi_activity);
        fLocation = (Location)getIntent().getExtras().getParcelable(KEY_LOCATION);
        fCancelButton = findViewById(R.id.btn_cancel);
        fCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick(v);
            }
        });
        fSaveButton = findViewById(R.id.btn_save);
        fSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });
        fName = (EditText)findViewById(R.id.etx_name);
        fComment = (EditText)findViewById(R.id.etx_comment);
        fRating = (RatingBar)findViewById(R.id.rbr_rating);
    }

    private void onSaveClick(View v) {
        PoiDetailBean detail = new PoiDetailBean();
        detail.setPoiName(fName.getText().toString());
        detail.setComment(fComment.getText().toString());
        detail.setRating(new Float(fRating.getRating()));
        detail.setLatitude(fLocation.getLatitude());
        detail.setLongitude(fLocation.getLongitude());
        PoiDetailSaver saver = new PoiDetailSaver(getApplicationContext(), this);
        Toast.makeText(this, R.string.saving, Toast.LENGTH_SHORT).show();
        fSaveButton.setEnabled(false);
        saver.save(detail);
    }

    private void onCancelClick(View v) {
        finish();
    }


    @Override
    public void onDetailSaved(Throwable th) {
        if (th != null) {
            fSaveButton.setEnabled(true);
            Toast.makeText(this, R.string.saving_failed, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, R.string.saving_done, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
