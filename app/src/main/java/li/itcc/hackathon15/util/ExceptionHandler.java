package li.itcc.hackathon15.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import li.itcc.hackathon15.util.loading.TaskAbortListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class ExceptionHandler implements TaskAbortListener {
    private final Context fContext;

    public ExceptionHandler(Context context) {
        fContext = context;
    }

    @Override
    public void onTaskAborted(Throwable th) {
        Log.e("ExceptionHandler", "Exception", th);
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
