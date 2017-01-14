package io.locative.app.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.locative.app.R;
import io.locative.app.model.Geofences;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.utils.Constants;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class GeofenceFragment extends ListFragment {

    public Geofences geofences = new Geofences();
    public static final String TAG = "fragment.geofences";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private boolean mLoading = true;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    public static GeofenceFragment newInstance(String param1, String param2) {
        GeofenceFragment fragment = new GeofenceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GeofenceFragment() {

    }

    public void refresh() {

        // Each row in the list stores country name, currency and flag
        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < Geofences.ITEMS.size(); i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            Geofences.Geofence geofence = Geofences.ITEMS.get(i);
            hm.put("image", "0");
            hm.put("title", geofence.name);
            hm.put("subtitle", "ID: " + geofence.getRelevantId());
            aList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {
                "image",
                "title",
                "subtitle"
        };

        // Ids of views in listview_layout
        int[] to = {
                R.id.image,
                R.id.title,
                R.id.subtitle
        };

        if (getActivity() != null) {
            GeofencesAdapter adapter = new GeofencesAdapter(getActivity().getBaseContext(), aList, R.layout.geofence_row, from, to);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName() + " must implement OnGeofenceSelection");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(Geofences.ITEMS.get(position).uuid);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLoading) {
            setListShown(true);
            refresh();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        if (!mLoading) {
            setListShown(true);
            refresh();
        }

        setEmptyText(getString(R.string.geofences_empty));
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
                //Get your item here with the position
                final int pos = position;
                final View aView = v;
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                ContentValues values = new ContentValues();
                                Geofences.Geofence item = Geofences.ITEMS.get(pos);

                                Log.i(Constants.LOG, "Deleting Item with pos: " + Long.toString(pos) + " custom_id: " + item.customId);
                                ContentResolver resolver = aView.getContext().getContentResolver();

                                resolver.delete(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), "custom_id = ?", new String[]{item.customId});
                                Geofences.ITEMS.remove(pos);
                                refresh();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Geofences.Geofence item = Geofences.ITEMS.get(position);
                Intent addEditGeofencesIntent = new Intent(getActivity(), AddEditGeofenceActivity.class);
                addEditGeofencesIntent.putExtra("geofenceId", item.customId);
                getActivity().startActivity(addEditGeofencesIntent);
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }
}