package io.locative.app.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import io.locative.app.R;

/**
 * Created by Jasper De Vrient on 2/05/2016.
 */
public class AddGeofenceDialogFragment extends DialogFragment implements AddGeofenceDialog {
    private AddGeofenceResultListener mLocallyListener;
    private AddGeofenceResultListener mImportListener;
    public static final String DIALOG_TAG = "AddGeofenceDialogFragment";

    @Override
    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, DIALOG_TAG);
    }

    public static AddGeofenceDialog createInstance() {
        return new AddGeofenceDialogFragment();
    }

    @Override
    public void setLocallyListener(AddGeofenceResultListener resultListener) {
        mLocallyListener = resultListener;
    }

    @Override
    public void setImportListener(AddGeofenceResultListener resultListener) {
        mImportListener = resultListener;
    }

    public interface AddGeofenceResultListener{
        public void onResult();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.geofence_add)
                .setMessage(R.string.geofence_add_message)
                .setPositiveButton(R.string.geofence_locally, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trigger(mLocallyListener);
                    }
                })
                .setNegativeButton(R.string.geofence_import, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trigger(mImportListener);
                    }
                });

        return builder.create();
    }

    private void trigger(AddGeofenceDialogFragment.AddGeofenceResultListener listener) {
        if (listener != null)
            listener.onResult();
    }
}

