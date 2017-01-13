package io.locative.app.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class Dialog {
    public static ProgressDialog getIndeterminateProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }
}
