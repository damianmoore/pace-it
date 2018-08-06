package uk.co.epixstudios.pace_it.paceit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class TrackActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "uk.co.epixstudios.pace_it.TRACK_ACTIVITY";

    TextView gps_position;
    TextView distance_text;
//    ArrayList<Location> locations = new ArrayList<Location>();
    Location prev_loc = null;
    float distance = 0;
    float total_distance = 0;
    Button button_start_stop;
    boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        gps_position = (TextView) findViewById(R.id.gps_position);
        distance_text = (TextView) findViewById(R.id.distance);
        button_start_stop = (Button) findViewById(R.id.button_start_stop);

        if (!running) {
            button_start_stop.setText("Start");
        }
        else {
            button_start_stop.setText("Stop");
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            new Intent(this, LocationPermissionActivity.class);
            return;
        } else {
            Log.v(TAG, "Have permission");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new TrackActivity.MyLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }

    public void onStartStop(View view) {
        if (!running) {
            running = true;
            total_distance = 0;
            button_start_stop.setText("Stop");
            distance_text.setText("" + total_distance);
        }
        else {
            running = false;
            button_start_stop.setText("Start");
            distance_text.setText("" + total_distance);
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            gps_position.setText("Lat: " + loc.getLatitude() + "\nLng: " + loc.getLongitude());

            if (prev_loc != null){

                distance = prev_loc.distanceTo(loc);
//                Toast.makeText(getApplicationContext(), " " + distance, Toast.LENGTH_SHORT).show();
                if (running) {
                    total_distance += distance;
                }

                Log.v(TAG, "" + total_distance);
                distance_text.setText("" + total_distance);

            }

            prev_loc = loc;
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
