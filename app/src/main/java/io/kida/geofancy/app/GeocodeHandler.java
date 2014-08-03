package io.kida.geofancy.app;

import android.os.Handler;
import android.os.Message;
import android.widget.Button;

import java.lang.ref.WeakReference;

/**
 * Created by mkida on 3/08/2014.
 */
public class GeocodeHandler extends Handler {
    public static final int UPDATE_ADDRESS = 1;
    public static final int SAVE_AND_FINISH = 2;

    private final WeakReference<AddEditGeofenceActivity> mTarget;

    public GeocodeHandler(AddEditGeofenceActivity target) {
        mTarget = new WeakReference<AddEditGeofenceActivity>(target);
    }

    @Override
    public void handleMessage(Message msg) {
        AddEditGeofenceActivity target = mTarget.get();
        switch (msg.what) {
            case UPDATE_ADDRESS: {
                Button button = (Button)target.findViewById(R.id.address_button);
                button.setText((String) msg.obj);
                break;
            }
            case SAVE_AND_FINISH: {
                if (target.mProgressDialog.isShowing()) {
                    target.mProgressDialog.hide();
                }
                target.save(true);
            }
        }

    }
}
