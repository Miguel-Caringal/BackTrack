package com.example.miguelcaringal.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mMagnetometer;
    long mCreationTime;
    private static final String INITIALIZATION_STATE = "INITIALIZATION_STATE";
    private static final String EXERCISE_STATE = "EXERCISE_STATE";
    private static final String TAG = "WL/MainActivity";
    private static final long INITIALIZATION_TIME = 3000;
    private static final float SQUAT_DEG_DIFF = 70;
    private static final float SQUAT_START_DEG_THRESHOLD = 20;
    float[] mGravity;
    float[] mGeomagnetic;
    float mInitDegsSum;
    float mNumInitDegs;
    String mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);    // Register the sensor listeners
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        // Store initial values
        mCreationTime = System.currentTimeMillis();
        mInitDegsSum = 0;
        mNumInitDegs = 0;
        mState = INITIALIZATION_STATE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
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
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime-mCreationTime;
        long deltaTimeThreshold = 500;
        
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

                updateInitDegs(pitch, deltaTime);

            }
        }
    }

    /**
     *
     * @param pitch
     * @param deltaTime
     */
    private void updateInitDegs (float pitch, long deltaTime) {

        if (deltaTime <= INITIALIZATION_TIME) {
            // Increment degs
            mInitDegsSum += pitch;
            mNumInitDegs++;
        } else if (mState.equals(INITIALIZATION_STATE)){
            // Calculate avgs
            float initDegAvg = mInitDegsSum / mNumInitDegs;
            Log.d(TAG, "initDegAvg=" + initDegAvg);
            playTone();
            mState = EXERCISE_STATE;
        }
    }

    private void playTone() {
        Log.d(TAG, "playTone");
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.ding);
        mediaPlayer.start();
    }
}
