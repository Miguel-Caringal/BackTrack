 package com.example.miguelcaringal.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    long prevTime;
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

    float[] mGravity;
    float[] mGeomagnetic;
    int topPadding = 0;
    int leftPadding = 0;

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
                    //Log.d("WL/MainActivity", "azimut="+ azimut);
                    Log.d(TAG, "pitch="+ pitch);
                    //Log.d("WL/MainActivity", "roll="+ roll);
                    /*


                     */
                }


                if (pitch > 0) {
                    topPadding = Math.max(topPadding-5, 0);
                } else if (pitch < 0) {
                    topPadding = Math.min(topPadding+5, 500);
                }

                if (roll > 0) {
                    leftPadding = Math.min(leftPadding+5, 500);

                } else if (roll < 0) {
                    leftPadding = Math.max(leftPadding-5, 0);
                }

            }
        }
    }
}
