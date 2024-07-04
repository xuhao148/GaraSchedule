package org.aya.garaschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GaraCommandAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<GaraItem> items;

    public GaraCommandAdapter(Context context, ArrayList<GaraItem> items) {
        this.ctx = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean isEnabled(int position) {
        return items.get(position).isEnabled();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).
                    inflate(R.layout.gara_menu, viewGroup, false);
        }

        // get current item to be displayed
        GaraItem currentItem = (GaraItem)getItem(i);

        // get the TextView for item name and item description
        TextView textViewItemName = (TextView)
                convertView.findViewById(R.id.gara_item);

        //sets the text for item name and item description from the current item object
        textViewItemName.setText(currentItem.getLabel());
        if (currentItem.isEnabled())
          textViewItemName.setTextColor(ctx.getResources().getColor(android.R.color.primary_text_dark_nodisable));
        else
            textViewItemName.setTextColor(ctx.getResources().getColor(android.R.color.darker_gray));
        // returns the view for the current row
        return convertView;
    }
}
