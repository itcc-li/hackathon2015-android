package li.itcc.hackathon15;

import android.content.Context;
import android.widget.Toast;

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
            Toast.makeText(fContext, "Done", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(fContext, "Exception " + th.getClass().getName() + " " + th.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
