package io.locative.app.view;

import android.app.ListFragment;
import android.os.Bundle;

/**
 * Created by Jasper De Vrient on 3/05/2016.
 */
public class FencelogsFragment extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GeofencesActivity ga = (GeofencesActivity)getActivity();
        ga.mGeofancyNetworkingWrapper.getFenceLogs(ga.mSessionManager.getSessionId());
    }
}
