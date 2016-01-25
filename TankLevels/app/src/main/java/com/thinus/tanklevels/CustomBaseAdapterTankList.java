package com.thinus.tanklevels;

/**
 * Created by thinus on 2015/11/25.
 */

import android.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import android.content.Context;
import android.widget.TextView;

public class CustomBaseAdapterTankList extends BaseAdapter {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static ArrayList<Tank> searchArrayList;

    private LayoutInflater mInflater;

    public CustomBaseAdapterTankList(Context context, ArrayList<Tank> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.tanklevel_list_item_view, null);
            holder = new ViewHolder();
            holder.txtTankID = (TextView) convertView.findViewById(R.id.tankID);
            holder.txtTimeStamp = (TextView) convertView.findViewById(R.id.timestamp);
            holder.txtPercentage = (TextView) convertView.findViewById(R.id.persentage);
            holder.txtVolume = (TextView) convertView.findViewById(R.id.volume);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtTankID.setText("Tank " + String.valueOf(searchArrayList.get(position).getTankID()));
        holder.txtTimeStamp.setText(searchArrayList.get(position).getTimeStamp());
        holder.txtPercentage.setText(String.valueOf(searchArrayList.get(position).getPersentage()) + "%");
        holder.txtVolume.setText(String.valueOf(searchArrayList.get(position).getVolume()) + "L (" + String.valueOf(searchArrayList.get(position).getValue()) + ")");

        return convertView;
    }

    static class ViewHolder {
        TextView txtTankID;
        TextView txtTimeStamp;
        TextView txtPercentage;
        TextView txtVolume;
    }

}
