package org.aya.garaschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleListAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<ScheduleItem> items;

    public ScheduleListAdapter(Context context, ArrayList<ScheduleItem> items) {
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
        return true;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).
                    inflate(R.layout.period_list, viewGroup, false);
        }

        // get current item to be displayed
        ScheduleItem currentItem = (ScheduleItem) getItem(i);

        // get the TextView for item name and item description
        TextView textViewItemName = (TextView)
                convertView.findViewById(R.id.course_name_indicator);
        TextView textViewItemInfo = (TextView)
                convertView.findViewById(R.id.course_info_indicator);
        TextView textViewItemNow = (TextView)
                convertView.findViewById(R.id.now_indicator);

        //sets the text for item name and item description from the current item object
        textViewItemName.setText(currentItem.item_name);
        textViewItemInfo.setText(currentItem.item_info);
        if (currentItem.on)
            textViewItemNow.setVisibility(View.VISIBLE);
        else
            textViewItemNow.setVisibility(View.INVISIBLE);
        // returns the view for the current row
        return convertView;
    }
}

class ScheduleItem {
    public String item_name;
    public String item_info;
    public boolean on;
}