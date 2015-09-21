package li.itcc.hackathon15;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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
            String stack = sw.toString();
            String text = th.getClass().getName() + " " + th.getMessage() + "\n" + stack;

            AlertDialog alertDialog = new AlertDialog.Builder(fContext).create();
            alertDialog.setTitle("Fehler");
            alertDialog.setMessage(text);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }
}
