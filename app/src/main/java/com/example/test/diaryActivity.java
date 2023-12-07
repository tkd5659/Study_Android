package com.example.test;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class diaryActivity extends AppCompatActivity implements OnTabItemSelectedListener, OnRequestListener, MyApplication.OnResponseListener {
    private static final String TAG = "MainActivity";

    Fragment1 fragment1;
    Fragment2 fragment2;


    BottomNavigationView bottomNavigation;

    Location currentLocation;
    GPSListener gpsListener;

    int locationCount = 0;
    String currentWeather;
    String currentAddress;
    String currentDateString;
    Date currentDate;
    SimpleDateFormat todayDateFormat;

    /**
     * 데이터베이스 인스턴스
     */
    public static NoteDatabase mDatabase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();



        setPicturePath();



        // 데이터베이스 열기
        openDatabase();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * 데이터베이스 열기 (데이터베이스가 없을 때는 만들기)
     */
    public void openDatabase() {
        // open database
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        mDatabase = NoteDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if (isOpen) {
            Log.d(TAG, "Note database is open.");
        } else {
            Log.d(TAG, "Note database is not open.");
        }
    }

    public void setPicturePath() {
        String folderPath = getFilesDir().getAbsolutePath();
        AppConstants.FOLDER_PHOTO = folderPath + File.separator + "photo";

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);
        if (!photoFolder.exists()) {
            photoFolder.mkdirs();
        }
    }

    public void onTabSelected(int position) {
        if (position == 0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();
        } else if (position == 1) {
            fragment2 = new Fragment2();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment2).commit();
        }
    }

    public void showFragment2(Note item) {

        fragment2 = new Fragment2();
        fragment2.setItem(item);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment2).commit();

    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void onRequest(String command) {
        if (command != null) {
            if (command.equals("getCurrentLocation")) {
                getCurrentLocation();
            }
        }
    }


    public void getCurrentLocation() {
        // set current time
        currentDate = new Date();

        //currentDateString = AppConstants.dateFormat3.format(currentDate);
        if (todayDateFormat == null) {
            todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
        }
        currentDateString = todayDateFormat.format(currentDate);
        AppConstants.println("currentDateString : " + currentDateString);

        if (fragment2 != null) {
            fragment2.setDateString(currentDateString);
        }


        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                String message = "Last Location -> Latitude : " + latitude + "\nLongitude:" + longitude;
                println(message);


                getCurrentAddress();
            }

            gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);

            println("Current location requested.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stopLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            manager.removeUpdates(gpsListener);

            println("Current location requested.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResponse(int requestCode, int responseCode, String response) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            currentLocation = location;

            locationCount++;

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String message = "Current Location -> Latitude : "+ latitude + "\nLongitude:"+ longitude;
            println(message);

            getCurrentAddress();
        }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    public void getCurrentAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            currentAddress = null;

            Address address = addresses.get(0);
            if (address.getLocality() != null) {
                currentAddress = address.getLocality();
            }

            if (address.getSubLocality() != null) {
                if (currentAddress != null) {
                    currentAddress +=  " " + address.getSubLocality();
                } else {
                    currentAddress = address.getSubLocality();
                }
            }

            String adminArea = address.getAdminArea();
            String country = address.getCountryName();
            println("Address : " + country + " " + adminArea + " " + currentAddress);

            if (fragment2 != null) {
                fragment2.setAddress(currentAddress);
            }
        }
    }




    private void println(String data) {
        Log.d(TAG, data);
    }

}