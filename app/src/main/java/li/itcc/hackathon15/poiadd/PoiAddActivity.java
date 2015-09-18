package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import li.itcc.hackathon15.R;
import li.itcc.hackathon15.ToastResultListener;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateResultBean;
import li.itcc.hackathon15.util.StreamUtil;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddActivity extends AppCompatActivity implements PoiAddSaver.PoiAddSaveProgressListener {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final int REQUEST_TAKE_PICTURE = 1;
    private static final int REQUEST_GET_GALLERY_PICTURE = 2;
    private static final int REQUEST_CROP_PICTURE = 3;
    public static final int FINAL_PICTURE_SIZE = 1080;
    private View fCancelButton;
    private View fSaveButton;
    private EditText fName;
    private EditText fDescription;
    private Location fLocation;
    private View fTakePictureButton;
    private ImageView fImage;
    private View fOpenGaleryButton;
    private File fLocalImageFileOriginal;
    private File fLocalImageFileCropped;
    private View fClearPictureButton;
    private ProgressBar fProgressBar;

    public static void start(Activity parent, Location location) {
        Intent i = new Intent(parent, PoiAddActivity.class);
        i.putExtra(KEY_LOCATION, location);
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
        fClearPictureButton = findViewById(R.id.viw_clear_picture_button);
        fClearPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearPictureClick(v);
            }
        });
        fName = (EditText)findViewById(R.id.etx_name);
        fDescription = (EditText)findViewById(R.id.etx_description);
        fImage = (ImageView)findViewById(R.id.img_image);
        fProgressBar = (ProgressBar)findViewById(R.id.prb_progress);
        fProgressBar.setMax(100);
        fProgressBar.setProgress(0);
        fProgressBar.setVisibility(View.INVISIBLE);
    }

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
        PoiCreateBean detail = new PoiCreateBean();
        detail.setPoiName(fName.getText().toString());
        detail.setPoiDescription(fDescription.getText().toString());
        if (fLocation != null) {
            detail.setLatitude(fLocation.getLatitude());
            detail.setLongitude(fLocation.getLongitude());
        }
        PoiAddSaver saver = new PoiAddSaver(getApplicationContext(), this);
        saver.setLocalImageFile(fLocalImageFileCropped);
        Toast.makeText(this, R.string.saving, Toast.LENGTH_SHORT).show();
        fSaveButton.setEnabled(false);
        fProgressBar.setVisibility(View.VISIBLE);
        saver.save(detail);
    }

    private void onCancelClick(View v) {
        finish();
    }

    //
    @Override
    public void onProgress(int percent) {
        fProgressBar.setProgress(percent);
    }

    @Override
    public void onDetailSaved(PoiCreateResultBean newBean, Throwable th) {
        if (th != null) {
            fSaveButton.setEnabled(true);
            fProgressBar.setVisibility(View.INVISIBLE);
            new ToastResultListener(this).onRefreshDone(th);
        }
        else {
            Toast.makeText(this, R.string.saving_done, Toast.LENGTH_SHORT).show();
            finish();
        }
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
