package com.example.left4candy.placeholdercustomer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private UserInfo userInfo;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        this.mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void renderWindowText(UserInfo uI, View view, Marker marker){
        String title = uI.getUserName();
        TextView tvTitle = view.findViewById(R.id.titleTextView);
        tvTitle.setText(title);

        String email = uI.getEmail();
        TextView tvEmail = view.findViewById(R.id.emailText);
        tvEmail.setText(email);

        String nr = uI.getPhoneNumber();
        TextView tvNr = view.findViewById(R.id.phoneText);
        tvNr.setText(nr);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(userInfo, mWindow, marker);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
