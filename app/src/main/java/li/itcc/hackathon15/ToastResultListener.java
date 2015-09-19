package li.itcc.hackathon15;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.widget.Toast;

import li.itcc.hackathon15.poilist.PoiRefresher;

/**
 * Created by Arthur on 12.09.2015.
 */
public class ToastResultListener implements PoiRefresher.RefreshDoneListener {
    private final Context fContext;

    public ToastResultListener(Context context) {
        fContext = context;
    }

    @Override
    public void onRefreshDone(Throwable th) {
        if (th == null) {
            Toast.makeText(fContext, R.string.done, Toast.LENGTH_SHORT).show();
        }
        else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            Toast.makeText(fContext, "Exception " + th.getClass().getName() + " " + th.getMessage() + "\n" + sw.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
