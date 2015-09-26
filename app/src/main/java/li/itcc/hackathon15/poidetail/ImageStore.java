package li.itcc.hackathon15.poidetail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;

/**
 * Created by Arthur on 21.09.2015.
 */
public class ImageStore {
    private final Context fContext;


    public class Key {
        private String fId;
        private long fTimeStamp;

        private Key(String uuid, long timeStamp) {
            fId = uuid;
            fTimeStamp = timeStamp;
        }
    }

    // constructor
    public ImageStore(Context context) {
        fContext = context;
    }

    public Key createKey(String uuid, long updateTime) {
        return new Key(uuid, updateTime);
    }

    public boolean exists(Key key) {
        File file = getFile(key);
        return file.exists() && file.canRead();
    }

    public OutputStream createImage(Key key) throws IOException {
        File file = getFile(key);
        return new FileOutputStream(file);
    }

    public String getImageFilePath(Key key) {
        return getFile(key).getAbsolutePath();
    }

    private File getFile(Key key) {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, "Flypostr_PoiImg_" + key.fId + "_" + key.fTimeStamp + ".jpg");
    }

}
