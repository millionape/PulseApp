package com.example.kittaphornj.pulseapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<String> strName;
    ArrayList<String> strMac;

    public CustomAdapter(Context context, ArrayList<String> strName, ArrayList<String> strMac) {
        this.mContext= context;
        this.strName = strName;
        this.strMac = strMac;
    }

    public int getCount() {
        return strName.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater mInflater =
                (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(view == null)
            view = mInflater.inflate(R.layout.row, parent, false);

        TextView blue_name = (TextView)view.findViewById(R.id.bname);
        TextView mac_addr = (TextView)view.findViewById(R.id.mac);
        blue_name.setText(strName.get(position));
        mac_addr.setText(strMac.get(position));

        return view;
    }
}
