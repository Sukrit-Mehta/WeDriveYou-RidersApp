package com.example.sukrit.ridersappwedriveyou.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.sukrit.ridersappwedriveyou.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by sukrit on 28/3/18.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomInfoWindow(Context context)
    {
        myView = LayoutInflater.from(context)
                .inflate(R.layout.custom_rider_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView txtPickupInfo = myView.findViewById(R.id.txtPickupInfo);
        txtPickupInfo.setText(marker.getTitle());
        TextView txtSnippet = myView.findViewById(R.id.txtSnippet);
        txtSnippet.setText(marker.getSnippet());
        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
