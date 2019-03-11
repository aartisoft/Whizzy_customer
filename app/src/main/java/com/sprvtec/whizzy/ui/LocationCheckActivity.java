package com.sprvtec.whizzy.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.sprvtec.whizzy.R;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

/**
 * Created by Sowjanya on 2/1/2019.
 */
public class LocationCheckActivity extends Activity {
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pager);

        findViewById(R.id.start).setOnClickListener(v -> startNewLocationUpdates());
        findViewById(R.id.schedule).setOnClickListener(v -> stopNewLocationUpdates());
        locationServiceStuff();


    }

    private void locationServiceStuff() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        if (mLastLocation == null)
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        else
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                    Log.e("Location updates", location.getAltitude() + "   " + location.getLongitude());
                    // Update UI with location data
                    // ...
                }
            }


        };

    }

    private void startNewLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.e("started", "start loc");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopNewLocationUpdates() {
        Log.e("stopped", "stop loc");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
