package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import li.itcc.hackathon15.PoiConstants;
import li.itcc.hackathon15.R;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.exactlocation.ExactLocationActivity;
import li.itcc.hackathon15.util.StreamUtil;
import li.itcc.hackathon15.util.ValidationHelper;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private DecimalFormat FORMAT_1 = new DecimalFormat("##0.000000");
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_EXACT_LOCATION = "KEY_EXACT_LOCATION";
    private static final int REQUEST_TAKE_PICTURE = 1;
    private static final int REQUEST_GET_GALLERY_PICTURE = 2;
    private static final int REQUEST_CROP_PICTURE = 3;
    private static final int REQUEST_EXACT_LOCATION = 4;
    public static final int FINAL_PICTURE_SIZE = 1080;
    private View fCancelButton;
    private View fSaveButton;
    private EditText fName;
    private EditText fDescription;
    private View fTakePictureButton;
    private ImageView fImage;
    private View fOpenGaleryButton;
    private File fLocalImageFileOriginal;
    private File fLocalImageFileCropped;
    private View fClearPictureButton;
    private GoogleApiClient fGoogleApiClient;
    private Location fLocation;
    private Location fExactLocation;
    private boolean fIsShowing;
    private boolean fIsRegistered;
    private TextView fLocationText;
    private View fExactLocationButton;

    public static void start(Activity parent) {
        Intent i = new Intent(parent, PoiAddActivity.class);
        parent.startActivityForResult(i, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        fLocalImageFileOriginal = new File(storageDir, "TEMP_Flypostr_Original.jpg");
        fLocalImageFileCropped = new File(storageDir, "TEMP_Flypostr_Cropped.jpg");
        if (savedInstanceState == null) {
            fLocalImageFileOriginal.delete();
            fLocalImageFileCropped.delete();
        }
        setContentView(R.layout.poi_add_activity);
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
        fClearPictureButton = findViewById(R.id.viw_clear_picture_button);
        fClearPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearPictureClick(v);
            }
        });
        fExactLocationButton = findViewById(R.id.viw_location_panel);
        fExactLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExactLocationClick(v);
            }
        });
        fLocationText = (TextView)findViewById(R.id.txv_location_text);
        fName = (EditText)findViewById(R.id.etx_name);
        fDescription = (EditText)findViewById(R.id.etx_description);
        fImage = (ImageView)findViewById(R.id.img_image);
        buildGoogleApiClient();
        // restore state
        if (savedInstanceState != null) {
            fLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            fExactLocation = savedInstanceState.getParcelable(KEY_EXACT_LOCATION);
        }
        updateLocationUI();
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
    protected void onPause() {
        super.onPause();
        setIsShowing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setIsShowing(true);
    }

    private void setIsShowing(boolean isShowing) {
        if (fIsShowing == isShowing) {
            return;
        }
        fIsShowing = isShowing;
        updateRegistration();
    }

    private void updateRegistration() {
        boolean shouldBeRegistered = fIsShowing && fGoogleApiClient.isConnected();
        if (fIsRegistered == shouldBeRegistered) {
            return;
        }
        if (shouldBeRegistered) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(fGoogleApiClient, locationRequest, this);
            fIsRegistered = true;
        }
        else {
            LocationServices.FusedLocationApi.removeLocationUpdates(fGoogleApiClient, this);
            fIsRegistered = false;
        }
    }

    private synchronized void buildGoogleApiClient() {
        Context context = this;
        fGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, fLocation);
        savedInstanceState.putParcelable(KEY_EXACT_LOCATION, fExactLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    // google api callbacks

    @Override
    public void onConnected(Bundle bundle) {
        //Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(fGoogleApiClient);
        //setLocation(lastKnownLocation);
        updateRegistration();
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
    }

    @Override
    public void onConnectionSuspended(int i) {
        updateRegistration();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        updateRegistration();
    }

    public void setLocation(Location location) {
        if (location == null) {
            return;
        }
        float newAccuracy = location.getAccuracy();
        if (newAccuracy == 0.0f || newAccuracy > 50.0f) {
            return;
        }
        fLocation = location;
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (fLocation == null) {
            fLocationText.setText(R.string.txt_your_location_gets_determined);
        }
        else {
            Location loc = fLocation;
            if (fExactLocation != null) {
                loc = fExactLocation;
            }
            String latitude = FORMAT_1.format(loc.getLatitude());
            String longitude = FORMAT_1.format(loc.getLongitude());
            String distance = Integer.toString((int)loc.getAccuracy());
            String text = getString(R.string.txt_lat_long_precision);
            text = MessageFormat.format(text, latitude, longitude, distance);
            fLocationText.setText(text);
        }
    }

    //

    private void onExactLocationClick(View v) {
        if (fLocation == null) {
            return;
        }
        ExactLocationActivity.start(this, fLocation, fExactLocation, REQUEST_EXACT_LOCATION);
    }

    // picture stuff

    private void onClearPictureClick(View v) {
        fLocalImageFileOriginal.delete();
        fLocalImageFileCropped.delete();
        updateImagePreview();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateImagePreview();
    }

    private void onOpenGaleryClick(View v) {
        getPictureFromGallery();
    }

    private void onTakePictureClick(View v) {
        takePicture();
    }

    private void onSaveClick(View v) {
        // validate input
        ValidationHelper vh = new ValidationHelper(this);
        String poiName = vh.validateText(fName, PoiConstants.POI_NAME_LENGTH_MIN, PoiConstants.POI_NAME_LENGTH_MAX);
        String poiDescription = vh.validateText(fDescription, PoiConstants.POI_COMMENT_LENGTH_MAX);
        if (vh.hasErrors()) {
            return;
        }
        if (fLocation == null) {
            Toast.makeText(this, R.string.txt_location_missing, Toast.LENGTH_LONG).show();
            return;
        }
        PoiCreateBean detail = new PoiCreateBean();
        detail.setPoiName(poiName);
        detail.setPoiDescription(poiDescription);
        detail.setLatitude(fLocation.getLatitude());
        detail.setLongitude(fLocation.getLongitude());
        if (fExactLocation != null) {
            detail.setExactLatitude(fExactLocation.getLatitude());
            detail.setExactLongitude(fExactLocation.getLongitude());
        }
        LocalPoiSaver saver = new LocalPoiSaver(getApplicationContext());
        // fire and forget
        saver.save(detail, fLocalImageFileCropped);
        finish();
    }

    private void onCancelClick(View v) {
        finish();
    }

    // getting pictures

    private void getPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        CharSequence text = getResources().getText(R.string.txt_select_picture);
        startActivityForResult(Intent.createChooser(intent, text), REQUEST_GET_GALLERY_PICTURE);
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the file uri where the photo should go
            Uri pictureUri = Uri.fromFile(fLocalImageFileOriginal);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivityForResult(intent, REQUEST_TAKE_PICTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == REQUEST_GET_GALLERY_PICTURE) {
                    Uri selectedImageUri = data.getData();
                    copyToLocalFile(selectedImageUri);
                    cropPicture();
                }
                else if (requestCode == REQUEST_TAKE_PICTURE) {
                    cropPicture();
                }
                else if (requestCode == REQUEST_CROP_PICTURE) {
                    updateImagePreview();
                }
                else if (requestCode == REQUEST_EXACT_LOCATION) {
                    fExactLocation = data.getExtras().getParcelable(ExactLocationActivity.RESULT_KEY);
                    updateLocationUI();
                }
            }
            catch (IOException x) {
                fLocalImageFileOriginal.delete();
                fLocalImageFileOriginal.delete();
                updateImagePreview();
            }
        }
    }

    private void updateImagePreview() {
        int addPictureButtonVisibility;
        int removePictureButtonVisibility;
        if (fLocalImageFileCropped.exists() && fLocalImageFileCropped.canRead()) {
            Bitmap bmp = BitmapFactory.decodeFile(fLocalImageFileCropped.getAbsolutePath());
            fImage.setImageBitmap(bmp);
            addPictureButtonVisibility = View.GONE;
            removePictureButtonVisibility = View.VISIBLE;
        }
        else {
            fImage.setImageBitmap(null);
            addPictureButtonVisibility = View.VISIBLE;
            removePictureButtonVisibility = View.GONE;
        }
        fTakePictureButton.setVisibility(addPictureButtonVisibility);
        fOpenGaleryButton.setVisibility(addPictureButtonVisibility);
        fClearPictureButton.setVisibility(removePictureButtonVisibility);
    }

    public void copyToLocalFile(Uri uri) throws IOException {
        boolean done = false;
        if( uri != null ) {
            // create local file
            if (Uri.fromFile(fLocalImageFileOriginal).equals(uri)) {
                return;
            }
            InputStream in = getContentResolver().openInputStream(uri);
            long totalSize = 0;
            if (in != null) {
                totalSize = copyToOutput(in);
            }
            if (totalSize == 0L) {
                // this might happen on older devices
                // try to retrieve the image from the media store first
                // this will only work for images selected from gallery
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    if (path != null) {
                        InputStream altIn = new FileInputStream(path);
                        long length = copyToOutput(altIn);
                        if (length > 0L) {
                            done = true;
                        }
                    }
                }
            }
            else {
                done = true;
            }
        }
        if (!done) {
            fLocalImageFileOriginal.delete();
        }
    }

    private long copyToOutput(InputStream in) throws IOException {
        File outFile = fLocalImageFileOriginal;
        FileOutputStream out = new FileOutputStream(outFile);
        long totalSize = StreamUtil.pumpAllAndClose(in, out);
        return totalSize;
    }

    private void cropPicture() throws IOException {
        if (!fLocalImageFileOriginal.exists()) {
            fLocalImageFileCropped.delete();
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            // fallback to automatic cropping
            SquareImageCropper cropper = new SquareImageCropper(this, fLocalImageFileCropped, FINAL_PICTURE_SIZE);
            cropper.crop(fLocalImageFileOriginal);
            updateImagePreview();
        }
        else {
            Uri pictureLocation = Uri.fromFile(fLocalImageFileOriginal);
            intent.setData(pictureLocation);
            intent.putExtra("outputX", FINAL_PICTURE_SIZE);
            intent.putExtra("outputY", FINAL_PICTURE_SIZE);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fLocalImageFileCropped));
            intent.putExtra("noFaceDetection", true);
            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, REQUEST_CROP_PICTURE);
        }
    }

}
