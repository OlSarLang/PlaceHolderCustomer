package com.example.left4candy.placeholdercustomer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import static com.example.left4candy.placeholdercustomer.Constants.MAPVIEW_BUNDLE_KEY;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";

    //String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName = "Not found";

    private DatabaseReference mDatabase;
    private DatabaseReference thisUserReference;
    private DatabaseReference thisUploadReference;
    private DatabaseReference thisMarkerReference;
    private StorageReference profileImageRef;
    private DatabaseReference userInfoRef;

    public List<UserInfo> userList;
    public List<Marker> markerList;
    public List<CustomMarker> thisUserCustomMarkerList;

    private Bitmap redMarker;
    private Bitmap greenMarker;
    private Bitmap blueMarker;
    private Bitmap yellowMarker;
    private Bitmap markerMap;

    //solid color markers
    private int height = 100;
    private int width = 100;

    private GoogleMap map;
    private MapView mMapView;
    public LatLngBounds bounds = new LatLngBounds(new LatLng(55.978793,10.336775), new LatLng(65.833435, 25.713965));

    public static MapsFragment newInstance(){
        return new MapsFragment();
    }

    public MapsFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        BitmapDrawable bitmapRed = (BitmapDrawable)getResources().getDrawable(R.drawable.colorred);
        BitmapDrawable bitmapGreen = (BitmapDrawable)getResources().getDrawable(R.drawable.colorgreen);
        BitmapDrawable bitmapBlue = (BitmapDrawable)getResources().getDrawable(R.drawable.colorblue);
        BitmapDrawable bitmapYellow = (BitmapDrawable)getResources().getDrawable(R.drawable.coloryellow);
        Bitmap red = bitmapRed.getBitmap();
        Bitmap green = bitmapGreen.getBitmap();
        Bitmap blue = bitmapBlue.getBitmap();
        Bitmap yellow = bitmapYellow.getBitmap();
        redMarker = Bitmap.createScaledBitmap(red, width, height, false);
        greenMarker = Bitmap.createScaledBitmap(green, width, height, false);
        blueMarker = Bitmap.createScaledBitmap(blue, width, height, false);
        yellowMarker = Bitmap.createScaledBitmap(yellow, width, height, false);

        userList = new ArrayList<>();
        markerList = new ArrayList<>();
        thisUserCustomMarkerList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                markerList.clear();
                for (final DataSnapshot ds : dataSnapshot.getChildren()){
                    thisUserReference = mDatabase.child(ds.getKey()).child("userinfo");
                    thisUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserInfo aU = dataSnapshot.getValue(UserInfo.class);
                            if(aU.isPrivacy() != true){
                                aU.setUserUid(ds.getKey());
                                checkMarkerImage(aU);
                                userList.add(aU);
                                Log.d(TAG, String.valueOf(userList.size()));
                                Log.d(TAG, "username : " + aU.getUserName() + "    phoneNumber: " + aU.getPhoneNumber());
                                Log.d(TAG, aU.getUserUid());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maps_fragment, container, false);
        mMapView = view.findViewById(R.id.mapView);

        initGoogleMap(savedInstanceState);
        return view;
    }
/*
    @SuppressLint("ClickableViewAccessibility")
    public void loadMarker(final CustomMarker customMarker){
        final LatLng markerPos = new LatLng(customMarker.getLatitude(), customMarker.getLongitude());

        Marker mkr;
        if(!customMarker.isSolid()){
            Picasso.get()
                    .load(customMarker.getImageUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            markerMap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                            Marker mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(getRoundedShape(markerMap))));
                            mkr.setDraggable(true);
                            userMarker.add(mkr);
                        }
                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }
        if(customMarker.isRed() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(redMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }else if(customMarker.isGreen() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(greenMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }else if(customMarker.isBlue() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(blueMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }else if(customMarker.isYellow() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(yellowMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }
    }*/

    public void loadImage(StorageReference ref){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        markerMap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        //changeProfileImage.setImageBitmap(getRoundedShape(markerMap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void checkMarkerImage(final UserInfo uI){
        DatabaseReference profileImageRef = mDatabase.child("/" + uI.getUserUid() + "/images/profile/");
        profileImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Upload up = dataSnapshot.getValue(Upload.class);
                Log.d(TAG, up.getMImageUrl());
                Picasso.get()
                        .load(up.getMImageUrl())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                markerMap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                                LatLng markerPos = new LatLng(uI.getLatitude(), uI.getLongitude());

                                Marker mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(redMarker)));
                                markerList.add(mkr);
                                Log.d(TAG, String.valueOf(markerList.size()));
                            }
                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            }
                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public Bitmap getRoundedShape(Bitmap scaleBitMapImage){
        Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) width- 1) / 2,
                ((float)height - 1) / 2,
                (Math.min(((float) width),
                        ((float) height)) / 2),
                Path.Direction.CCW);
        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitMapImage;
        canvas.drawBitmap(sourceBitmap, new Rect(0,0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);
        return targetBitmap;
    }



    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map = googleMap;
        map.setLatLngBoundsForCameraTarget(bounds);
        map.setMinZoomPreference(5);
        map.moveCamera(CameraUpdateFactory.newLatLng(bounds.getCenter()));

        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setMyLocationEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                UserInfo userInfo = userList.get(markerList.indexOf(marker));
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(userInfo.getUserName()).setMessage(getResources().getString(R.string.Contact) + ": " + userInfo.getEmail() + "\n" + getResources().getString(R.string.Phone) + ": " + userInfo.getPhoneNumber()).setPositiveButton("hej", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
