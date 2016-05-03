package io.locative.app.view;

import android.app.FragmentManager;

/**
 * Created by Jasper De Vrient on 2/05/2016.
 */
public interface AddGeofenceDialog {
    public void show(FragmentManager fragmentManager);
    public void setLocallyListener(AddGeofenceDialogFragment.AddGeofenceResultListener resultListener);
    public void setImportListener(AddGeofenceDialogFragment.AddGeofenceResultListener resultListener);
}