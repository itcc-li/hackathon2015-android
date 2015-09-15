package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.ToastResultListener;
import li.itcc.hackathon15.services.PoiDetailBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddActivity extends AppCompatActivity implements PoiAddSaver.PoiAddSaveDoneListener {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final int REQUEST_TAKE_PICTURE = 1;
    private static final int REQUEST_GET_GALARY_PICTURE = 2;
    private static final int REQUEST_CROP_PICTURE = 3;
    private static final int RESULT_IO_EXCEPTION = RESULT_FIRST_USER + 1;


    private View fCancelButton;
    private View fSaveButton;
    private EditText fName;
    private EditText fComment;
    private RatingBar fRating;
    private Location fLocation;
    private View fTakePictureButton;
    private ImageView fImage;
    private View fOpenGaleryButton;
    // persistent varialbles
    private Uri fNextPhotoUri;
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
        fOpenGaleryButton = findViewById(R.id.viw_open_galery_button);
        fOpenGaleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOpenGaleryClick(v);
            }
        });
        fName = (EditText)findViewById(R.id.etx_name);
        fComment = (EditText)findViewById(R.id.etx_comment);
        fRating = (RatingBar)findViewById(R.id.rbr_rating);
        fImage = (ImageView)findViewById(R.id.img_image);
    }

    private void onOpenGaleryClick(View v) {
        getPictureFromGalary();
    }

    private void onTakePictureClick(View v) {
        takePicture();
    }

    private void onSaveClick(View v) {
        PoiDetailBean detail = new PoiDetailBean();
        detail.setPoiName(fName.getText().toString());
        detail.setComment(fComment.getText().toString());
        detail.setRating(new Float(fRating.getRating()));
        if (fLocation != null) {
            detail.setLatitude(fLocation.getLatitude());
            detail.setLongitude(fLocation.getLongitude());
        }
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
            new ToastResultListener(this).onRefreshDone(th);
        }
        else {
            Toast.makeText(this, R.string.saving_done, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // getting a picture

    private void getPictureFromGalary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_GALARY_PICTURE);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file uri where the photo should go
            try {
                fNextPhotoUri = createTempImageUri();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fNextPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
            }
            catch (Exception x) {
                onActivityResult(REQUEST_TAKE_PICTURE, RESULT_IO_EXCEPTION, null);
            }
        }
    }

    private File createTempImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File nextPhotoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return nextPhotoFile;
    }


    private Uri createTempImageUri() throws IOException {
        File tempFile = createTempImageFile();
        return Uri.fromFile(tempFile);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == REQUEST_GET_GALARY_PICTURE) {
                    Uri selectedImageUri = data.getData();
                    Uri localUri = copyToLocalFile(selectedImageUri);
                    cropPicture(localUri);
                }
                else if (requestCode == REQUEST_TAKE_PICTURE) {
                    Uri selectedImageUri = fNextPhotoUri;
                    cropPicture(selectedImageUri);
                }
                else if (requestCode == REQUEST_CROP_PICTURE) {
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
            catch (IOException x) {
                resultCode = RESULT_IO_EXCEPTION;
            }
        }
        if (resultCode != RESULT_OK) {
            // TODO: error handling
        }
    }

    public Uri copyToLocalFile(Uri uri) throws IOException {
        if( uri == null ) {
            return null;
        }
        if (uri.getHost().equals("file")) {
            // already local
            return uri;
        }
        // create local file
        InputStream in = getContentResolver().openInputStream(uri);

        File outFile = createTempImageFile();
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
        in.close();
        out.close();
        return Uri.fromFile(outFile);
    }

    private void cropPicture(Uri pictureLocation) {
        // this does not work properly
        // see http://stackoverflow.com/questions/12758425/how-to-set-the-output-image-use-com-android-camera-action-crop
        //
        // no official api
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            intent.setData(pictureLocation);
            intent.putExtra("outputX", 1080); // does not work, it returns a smaller size
            intent.putExtra("outputY", 1080);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            intent.putExtra("noFaceDetection", true);
            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, REQUEST_CROP_PICTURE);
        }
    }

}
