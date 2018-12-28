package com.example.left4candy.placeholdercustomer;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class AdminUser {
    private LatLng location;
    private boolean privacy;
    private String userName;

    public AdminUser(LatLng latLng, String userName, boolean privacy){
        this.location = latLng;
        this.userName = userName;
        this.privacy = privacy;
    }

    public LatLng getLocation() {
        return location;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public String getUserName() {
        return userName;
    }
}
