package uk.co.epixstudios.pace_it.paceit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import static android.content.ContentValues.TAG;


public class StartActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "uk.co.epixstudios.pace_it.START_ACTIVITY";

    TextView distance;
    TextView time;
    TextView speed;
    TextView pace;
    TextView gps_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Spinner spinner = (Spinner) findViewById(R.id.distance_unit_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.distance_units_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        distance = (TextView) findViewById(R.id.distance_text);
        time = (TextView) findViewById(R.id.time_text);
        speed = (TextView) findViewById(R.id.speed_text);
        pace = (TextView) findViewById(R.id.pace_text);

        distance.addTextChangedListener(new MyTextWatcher());
        time.addTextChangedListener(new MyTextWatcher());
        speed.addTextChangedListener(new MyTextWatcher());
        pace.addTextChangedListener(new MyTextWatcher());

        distance.requestFocus();
    }

    public void nextActivity(View view) {
        Intent intent = new Intent(this, LocationPermissionActivity.class);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            intent = new Intent(this, TrackActivity.class);
        }

        Button editText = (Button) findViewById(R.id.button);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.i(TAG, s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    }
}
