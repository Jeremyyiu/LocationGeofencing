package io.locative.app.view;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.*;

import javax.inject.Inject;

import io.locative.app.R;
import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.network.LocativeNetworkingCallback;
import io.locative.app.network.SessionManager;

/**
 * Created by Jasper De Vrient on 2/05/2016.
 */
public class ImportGeofenceFragment extends ListFragment {
    private static final String IMAGE_KEY = "image",
        TITLE_KEY = "title",
        SUBTITLE_KEY = "subtitle",
        IMAGE_VAL_DEFAULT = "0";
    // Keys used in Hashmap
    private static final String[] FROM = {
            "image",
            "title",
            "subtitle"
    };

    // Ids of views in listview_layout
    private static final int[] TO = {
            R.id.image,
            R.id.title,
            R.id.subtitle
    };
    private List<Geofences.Geofence> mFences;
    private OnGeofenceSelection mListener;

    public void refresh() {
        List<HashMap<String, String>> aList = new ArrayList<>();
        for (Geofences.Geofence geofence : mFences) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put(IMAGE_KEY, IMAGE_VAL_DEFAULT);
            hm.put(TITLE_KEY, geofence.title);
            hm.put(SUBTITLE_KEY, geofence.subtitle);
            aList.add(hm);
        }
        GeofencesAdapter adapter = new GeofencesAdapter(getActivity().getBaseContext(), aList, R.layout.geofence_row, FROM, TO);
        setListAdapter(adapter);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnGeofenceSelection) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGeofenceSelection");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GeofencesActivity ga = (GeofencesActivity) getActivity();
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    Geofences.Geofence item = mFences.get(position);
                    mListener.onFragmentInteraction(item);
                }
            }
        });

        ga.mGeofancyNetworkingWrapper.getGeofences(ga.mSessionManager.getSessionId(), new LocativeNetworkingCallback() {
            @Override
            public void onLoginFinished(boolean success, String sessionId) {

            }

            @Override
            public void onSignupFinished(boolean success, boolean userAlreadyExisting) {

            }

            @Override
            public void onCheckSessionFinished(boolean sessionValid) {

            }

            @Override
            public void onDispatchFencelogFinished(boolean success) {

            }

            @Override
            public void onGetGeoFencesFinished(List<Geofences.Geofence> fences) {
                mFences = fences;
                refresh();
            }

            @Override
            public void onGetFencelogsFinished(List<Fencelog> fencelogs) {

            }
        });
    }

    public interface OnGeofenceSelection {
        public void onFragmentInteraction(Geofences.Geofence fence);
    }
}
