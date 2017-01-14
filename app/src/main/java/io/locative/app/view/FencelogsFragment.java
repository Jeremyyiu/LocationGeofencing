package io.locative.app.view;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.network.LocativeNetworkingAdapter;
import io.locative.app.utils.Constants;

class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

    private WeakReference<FencelogsFragment> mFragment;

    public ListFragmentSwipeRefreshLayout(FencelogsFragment fragment) {
        super(fragment.getActivity());
        mFragment = new WeakReference<>(fragment);
    }

    /**
     * We need to override this method to properly signal when a
     * 'swipe-to-refresh' is possible.
     *
     * @return true if the {@link android.widget.ListView} is visible and can scroll up.
     */
    @Override
    public boolean canChildScrollUp() {
        final ListView listView = mFragment.get().getListView();
        if (listView.getVisibility() == View.VISIBLE) {
            return canListViewScrollUp(listView);
        } else {
            return false;
        }
    }

    private static boolean canListViewScrollUp(ListView listView) {
        return ViewCompat.canScrollVertically(listView, -1);
    }
}

public class FencelogsFragment extends ListFragment {
    public static final String TAG = "fragment.fencelogs";
    private final DateTimeFormatter TODAYFORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter WEEKFORMATTER = DateTimeFormatter.ofPattern("EE, HH:mm");
    private final DateTimeFormatter THISMONTHFORMATTER = DateTimeFormatter.ofPattern("d LLL, HH:mm");
    private final DateTimeFormatter MONTHFORMATTER = DateTimeFormatter.ofPattern("d LLL, HH:mm");
    private final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("d-MM-uuuu, HH:mm");
    private static final String
            TITLE_KEY = "title",
            ORIGIN_KEY = "origin",
            DATE_KEY = "date";
    // Keys used in Hashmap
    private static final String[] FROM = {
            TITLE_KEY,
            ORIGIN_KEY,
            DATE_KEY
    };

    // Ids of views in listview_layout
    private static final int[] TO = {
            R.id.title,
            R.id.origin,
            R.id.date
    };
    private static final String ENTER = "entered",
        EXIT = "left";
    private List<Fencelog> mFences;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create the list fragment's content view by calling the super method
        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(this);

        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;
    }

    public void refresh() {
        if (getActivity() == null) {
            return;
        }
        List<HashMap<String, String>> fenceEntries = new ArrayList<>();
        for (Fencelog fencelog : mFences) {
            HashMap<String, String> entry = new HashMap<>(FROM.length);
            entry.put(TITLE_KEY, fencelog.locationId + " " + getVerb(fencelog.eventType));
            entry.put(ORIGIN_KEY, fencelog.origin);
            entry.put(DATE_KEY, makeDateEntry(fencelog.createdAt));
            fenceEntries.add(entry);
        }
        setListAdapter(new SimpleAdapter(getActivity().getBaseContext(), fenceEntries, R.layout.fencelog_row, FROM, TO));
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private String getVerb(EventType type) {
        if (type == EventType.ENTER)
            return ENTER;
        return EXIT;
    }

    private String makeDateEntry(LocalDateTime date) {
        if (date == null)
            return "";
        LocalDateTime today = LocalDateTime.now();
        if (today.toLocalDate().equals(date.toLocalDate()))
            return date.format(TODAYFORMATTER);
        if (today.minusDays(today.getDayOfWeek().getValue()).isBefore(date))
            return date.format(WEEKFORMATTER);
        if (today.minusDays(today.getDayOfMonth()).isBefore(date))
            return date.format(THISMONTHFORMATTER);
        if (today.getYear() == date.getYear())
            return date.format(MONTHFORMATTER);
        return date.format(DATEFORMATTER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.fencelogs_empty));
        GeofencesActivity ga = (GeofencesActivity)getActivity();
        ga.mFabButton.hide();
        ga.mLocativeNetworkingWrapper.getFenceLogs(ga.mSessionManager.getSessionId(), Constants.FENCELOG_LIMIT, new LocativeNetworkingAdapter() {
            @Override
            public void onGetFencelogsFinished(List<Fencelog> fencelogs) {
                mFences = fencelogs;
                refresh();
            }
        });
    }
}
