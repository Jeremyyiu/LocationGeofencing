package io.locative.app.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import io.locative.app.R;

/**
 * Created by mkida on 4/08/2014.
 */
public class GeofencesAdapter extends SimpleAdapter {
    public GeofencesAdapter(Context context, List<? extends Map<String, String>> data,
                            int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setImageResource(R.drawable.ic_launcher);

        // TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
        // subtitle.setText("ID: " + subtitle.getText().toString());

        return view;
    }

}
