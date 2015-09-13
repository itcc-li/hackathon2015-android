package li.itcc.hackathon15.poiadd;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.services.PoiDetailBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddActivity extends AppCompatActivity implements PoiAddSaver.PoiAddSaveDoneListener {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private View fCancelButton;
    private View fSaveButton;
    private EditText fName;
    private EditText fComment;
    private RatingBar fRating;
    private Location fLocation;
    private View fTakePictureButton;
    private ImageView fImage;
    private Uri fNextPhotoUri;
    private File fNextPhotoFile;
    private Bitmap fBitmap;

    public static void start(Activity parent, Location location) {
        Intent i = new Intent(parent, PoiAddActivity.class);
        i.putExtra(KEY_LOCATION, location);
        parent.startActivityForResult(i, 0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_add_activity);
        fLocation = getIntent().getExtras().getParcelable(KEY_LOCATION);
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
        fTakePictureButton = findViewById(R.id.viw_take_picture_button);
        fTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTakePictureClick(v);
            }
        });
        fName = (EditText) findViewById(R.id.etx_name);
        fComment = (EditText) findViewById(R.id.etx_comment);
        fRating = (RatingBar) findViewById(R.id.rbr_rating);
        fImage = (ImageView) findViewById(R.id.img_image);
    }

    private void onTakePictureClick(View v) {
        takePicture();
    }

    private void onSaveClick(View v) {
        PoiDetailBean detail = new PoiDetailBean();
        detail.setPoiName(fName.getText().toString());
        detail.setComment(fComment.getText().toString());
        detail.setRating(new Float(fRating.getRating()));
        detail.setLatitude(fLocation.getLatitude());
        detail.setLongitude(fLocation.getLongitude());
        PoiAddSaver saver = new PoiAddSaver(getApplicationContext(), this);
        Toast.makeText(this, R.string.saving, Toast.LENGTH_SHORT).show();
        fSaveButton.setEnabled(false);
        saver.save(detail, fBitmap);
    }

    private void onCancelClick(View v) {
        finish();
    }


    @Override
    public void onDetailSaved(Throwable th) {
        if (th != null) {
            fSaveButton.setEnabled(true);
            Toast.makeText(this, R.string.saving_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.saving_done, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // taking picture

    private void createNextFotoFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        try {
            fNextPhotoFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            fNextPhotoUri = Uri.fromFile(fNextPhotoFile);
        } catch (IOException e) {
            fNextPhotoFile = null;
            fNextPhotoUri = null;
        }

    }

    static final int REQUEST_TAKE_PICTURE = 1;
    static final int REQUEST_CROP_PICTURE = 2;

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            // Continue only if the File was successfully created
            createNextFotoFile();
            if (fNextPhotoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fNextPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
            }
            else {
                // can not create file
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PICTURE && resultCode == RESULT_OK) {
            cropPicture();
        }
        else if (requestCode == REQUEST_CROP_PICTURE && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = extras.getParcelable("data");
                    fBitmap = bitmap;
                    fImage.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void cropPicture() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            intent.setData(fNextPhotoUri);
            intent.putExtra("outputX", 1080);
            intent.putExtra("outputY", 1080);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, REQUEST_CROP_PICTURE);
        }
    }

}
