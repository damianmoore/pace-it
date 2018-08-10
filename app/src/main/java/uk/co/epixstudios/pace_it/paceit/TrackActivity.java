package uk.co.epixstudios.pace_it.paceit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class TrackActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "uk.co.epixstudios.pace_it.TRACK_ACTIVITY";

    SharedPreferences settings;
    float target_distance = 0;
    float target_time = 0;

    TextView gps_position;
    TextView distance_text;
    TextView time_elapsed_text;
    TextView speed_current;
    TextView speed_overall;
    ProgressBar progress_pace;
    ProgressBar progress_total;

    Location prev_loc = null;
    float distance = 0;
    float total_distance = 0;
    Button button_start_stop;
    boolean running = false;
    long start_time;
    long finish_time;
    long prev_loc_time = 0;
    Timer timer_clock;
    Timer timer_stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        settings = getSharedPreferences("app_preferences", 0);
        target_distance = settings.getInt("distance", 1000);
        target_time = settings.getInt("time", 1000);

        gps_position = (TextView) findViewById(R.id.gps_position);
        distance_text = (TextView) findViewById(R.id.distance);
        time_elapsed_text = (TextView) findViewById(R.id.time_elapsed);
        speed_current = (TextView) findViewById(R.id.speed_current);
        speed_overall = (TextView) findViewById(R.id.speed_overall);
        button_start_stop = (Button) findViewById(R.id.button_start_stop);
        progress_pace = (ProgressBar) findViewById(R.id.progress_pace);
        progress_total = (ProgressBar) findViewById(R.id.progress_total);

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        }
    }

    public void onStartStop(View view) {
        if (!running) {
            startTracking();
        }
        else {
            stopTracking();
        }
    }

    private void startTracking() {
        if (!running) {
            running = true;
            total_distance = 0;
            start_time = System.currentTimeMillis();
            finish_time = start_time + settings.getInt("time", 0) * 1000;
            button_start_stop.setText("Stop");
            distance_text.setText(String.format("%.0fm", total_distance));
            progress_total.setProgress(0);
            runClock();
            runStats();
        }
    }

    private void stopTracking() {
        if (running) {
            running = false;
            button_start_stop.setText("Start");
            distance_text.setText(String.format("%.0fm", total_distance));
            stopClock();
        }
    }

    private void runClock() {
        timer_clock = new Timer();
        timer_clock.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long delta_time = System.currentTimeMillis() - start_time;
                        displayClock(delta_time);
                    }
                });
            }
        }, 0, 29);
    }

    private void stopClock() {
        timer_clock.cancel();
        long delta_time = System.currentTimeMillis() - start_time;
        displayClock(delta_time);
    }

    private void displayClock(long millis) {
        time_elapsed_text.setText(String.format("%02d:%02d:%02d", millis / 60000, (millis / 1000) % 60, (millis / 10) % 100));
    }

    private void runStats() {
        timer_stats = new Timer();
        timer_stats.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayStats();
                        checkClock();
                    }
                });
            }
        }, 500, 500);
    }

    private void displayStats() {
        if (running) {
            // Calculate overall speed
            float elapsed_time_overall = prev_loc_time - start_time;
            float speed_m_per_s_overall = (total_distance / elapsed_time_overall) * 1000;
            float speed_km_per_h_overall = (speed_m_per_s_overall * 3600) / 1000;
            if (speed_m_per_s_overall > 0) {
                speed_overall.setText("" + String.format("%.2f", speed_km_per_h_overall) + "km/h, " + String.format("%.2f", speed_m_per_s_overall) + "m/s (overall)");
            }
        }
    }

    private void checkClock() {
        if (System.currentTimeMillis() >= finish_time) {
            stopTracking();
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            gps_position.setText("Lat: " + loc.getLatitude() + "\nLng: " + loc.getLongitude());
            speed_current.setText("" + loc.getSpeed() + "m/s");

            if (loc.getAccuracy() < 20 || Build.FINGERPRINT.contains("generic")) {  // Don't require accuracy if running in emulator
                long current_time = System.currentTimeMillis();
                if (prev_loc != null) {
                    button_start_stop.setEnabled(true);
                    distance = prev_loc.distanceTo(loc);
//                    Toast.makeText(getApplicationContext(), " " + distance, Toast.LENGTH_SHORT).show();
                    if (running) {
                        total_distance += distance;
                        Log.v(TAG, "" + total_distance);
                        distance_text.setText(String.format("%.0fm", total_distance));
                        progress_total.setProgress(Math.round((total_distance / target_distance) * 100));

                        // Calculate current speed
                        float elapsed_time = current_time - prev_loc_time;
                        float speed_m_per_s = (distance / elapsed_time) * 1000;
                        float speed_km_per_h = (speed_m_per_s * 3600) / 1000;
                        speed_current.setText("" + String.format("%.2f", speed_km_per_h) + "km/h, " + String.format("%.2f", speed_m_per_s) + "m/s (current)");
                    }
                }
                prev_loc = loc;
                prev_loc_time = System.currentTimeMillis();
            }
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
