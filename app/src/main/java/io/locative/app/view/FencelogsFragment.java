package io.locative.app.view;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.network.LocativeNetworkingAdapter;
import io.locative.app.utils.Constants;

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

    public void refresh() {
        if (getActivity() != null) {
            List<HashMap<String, String>> fenceEntries = new ArrayList<>();
            for (Fencelog fencelog : mFences) {
                HashMap<String, String> entry = new HashMap<>(FROM.length);
                entry.put(TITLE_KEY, fencelog.locationId + " " + getVerb(fencelog.eventType));
                entry.put(ORIGIN_KEY, fencelog.origin);
                entry.put(DATE_KEY, makeDateEntry(fencelog.createdAt));
                fenceEntries.add(entry);
            }
            setListAdapter(new SimpleAdapter(getActivity().getBaseContext(), fenceEntries, R.layout.fencelog_row, FROM, TO));
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
