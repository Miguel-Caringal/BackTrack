package com.example.miguelcaringal.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    long prevTime;
    float[] mGravity;
    float[] mGeomagnetic;
    private static final String TAG = "WL/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);    // Register the sensor listeners
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimut = (float) Math.toDegrees(orientation[0]);
                float pitch = (float) Math.toDegrees(orientation[1]);
                float roll = (float) Math.toDegrees(orientation[2]);// orientation contains: azimut, pitch and roll
                long nowTime = System.currentTimeMillis();
                long deltaTime = nowTime-prevTime;
                long deltaTimeThreshold = 500;

                if (deltaTime > deltaTimeThreshold) {
                    prevTime = nowTime;
                    // Log.d("WL/MainActivity", "azimut="+ azimut);
                    // Log.d(TAG, "pitch="+ pitch);
                    // Log.d("WL/MainActivity", "roll="+ roll);
                }
            }
        }
    }


    public void begin(View view) {
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

    public void startSensorActivity(View view) {
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }
}
