package com.home.croaton.followme.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.home.croaton.followme.R;
import com.home.croaton.followme.domain.ExcursionBrief;

import java.util.ArrayList;

public class ExcursionBriefAdapter extends ArrayAdapter<ExcursionBrief> {
    private final Context context;
    private final ArrayList<ExcursionBrief> items;

    public ExcursionBriefAdapter(Context context, int textViewResourceId, ArrayList<ExcursionBrief> items) {
        super(context, textViewResourceId, items);

        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.excursion_brief_item, null);
        }
        ExcursionBrief excursion = items.get(position);
        if (excursion != null) {
            ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.thumbnail);
            TextView nameText = (TextView) convertView.findViewById(R.id.name);
            TextView costText = (TextView) convertView.findViewById(R.id.cost);

            if (thumbnailView != null) {
                int imageId = getImageId(excursion.getThumbnailFilePath());
                thumbnailView.setImageResource(imageId);
            }
            if (nameText != null) {
                nameText.setText(excursion.getName());
            }
            if (costText != null)
            {
                if (excursion.getCost() == 0.0)
                    costText.setText(R.string.cost_free);
                else
                    costText.setText(String.valueOf(excursion.getCost()) + R.string.euro);
            }
        }
        return convertView;
    }

    @Override
    public ExcursionBrief getItem(int position) {
        return items.get(position);
    }

    private int getImageId(String imageName) {
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }
}
