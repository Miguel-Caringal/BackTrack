package com.example.miguelcaringal.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mMagnetometer;
    float[] mGravity;
    float[] mGeomagnetic;
    long mCreationTime;
    float mInitDegsSum;
    float mNumInitDegs;
    String mState;
    float mInitDegAvg;
    long mLastStateChangeTime;
    ArrayList<Long> mStateChangeTimeIntervals;
    MediaPlayer mediaPlayer;
    long timeOfLastMovt;
    float lastBigMovtDeg;
    long downMovtAvgTime, upMovtAvgTime;
    long lastLogTime;
    ArrayList<Integer> timeData;
    ArrayList<Float> pitchData;

    private static final String INITIALIZATION_STATE = "INITIALIZATION_STATE";
    private static final String UP_STATE = "UP_STATE";
    private static final String DOWN_STATE = "DOWN_STATE";
    private static final String END_STATE = "END_STATE";
    private static final String TAG = "WL/MainActivity";
    private static final long WAIT_TIME = 3000;
    private static final long INITIALIZATION_TIME = 5000;
    private static final float DOWN_STATE_DEG = 30;
    private static final long LOG_INTERVAL = 500;
    private static final float DEG_ERROR = 5;
    private static final long END_TIME_DETECT_THRESHOLD = 5000;

    public String exerciseType = "";
    private static final String PITCH_DATA_KEY = "PITCH_DATA_KEY";
    private static final String TIME_DATA_KEY = "TIME_DATA_KEY";

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
        mStateChangeTimeIntervals = new ArrayList<>();
        lastLogTime = mCreationTime;

        Bundle bundle = getIntent().getExtras();

        if(bundle.getString("exercise")!= null)
        {
            //TODO here get the string stored in the string variable and do

            exerciseType = bundle.getString("exercise");

            TextView display = findViewById(R.id.display);

            display.setText(exerciseType.toUpperCase() + "!");

        }

        timeData = new ArrayList<>();
        pitchData = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

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
                long deltaTime = nowTime-mCreationTime;



                updateInitDegs(pitch, nowTime);
            }
        }
    }

    /**
     *
     * @param pitch
     * @param nowTime
     */
    private void updateInitDegs (float pitch, long nowTime) {

        float pitchDelta = Math.abs(pitch - mInitDegAvg);
        long creationDeltaTime = nowTime - mCreationTime;

        if (nowTime - lastLogTime >= LOG_INTERVAL) {
            lastLogTime = nowTime;
            timeData.add((int) creationDeltaTime);
            pitchData.add(pitchDelta);

            if (mState.equals(END_STATE)) {
                Log.d(TAG, "END_STATE");
            } else {
                Log.d(TAG, "pitchDelta=" + pitchDelta);
            }
        }

        if (!mState.equals(INITIALIZATION_STATE)) {
            detectEndExercise(pitch, nowTime);
        }

        if (mState.equals(END_STATE)) {

            downMovtAvgTime = getEvenIndexAvg(mStateChangeTimeIntervals);
            upMovtAvgTime = getOddIndexAvg(mStateChangeTimeIntervals);

            Intent intent = new Intent(this, ReviewActivity.class);
            intent.putExtra("exercise", exerciseType);

            intent.putExtra(PITCH_DATA_KEY, convertFloats(pitchData));
            intent.putExtra(TIME_DATA_KEY, convertIntegers(timeData));

            startActivity(intent);
            //Log.d(TAG, END_STATE);
            //Log.d(TAG, "downMovtAvgTime=" + downMovtAvgTime);
           // Log.d(TAG, "upMovtAvgTime=" + upMovtAvgTime);

            return;
        }

        if (creationDeltaTime <= WAIT_TIME) {
            return;
        }

        if (creationDeltaTime <= INITIALIZATION_TIME + WAIT_TIME) {
            // Increment degs
            Log.d(TAG, "INIT: pitch=" + pitch);
            mInitDegsSum += pitch;
            mNumInitDegs++;
        } else if (mState.equals(INITIALIZATION_STATE)){

            // Calculate avgs
            mInitDegAvg = mInitDegsSum / mNumInitDegs;
            playTone();
            mState = UP_STATE;
            mLastStateChangeTime = nowTime;
            lastBigMovtDeg = mInitDegAvg;
            timeOfLastMovt = nowTime;

            Log.d(TAG, "INITIALIZATION_STATE");
            Log.d(TAG, "mInitDegAvg" + mInitDegAvg);
        } else if (mState.equals(UP_STATE) && pitchDelta > DOWN_STATE_DEG) {
            Log.d(TAG, "CHANGE TO DOWN STATE");
            Log.d(TAG, "pitch=" + pitch + " pitchDelta=" + pitchDelta);

            long stateChangeDeltaTime = nowTime - mLastStateChangeTime;

            // Down movt times is evens
            mStateChangeTimeIntervals.add(stateChangeDeltaTime);
            playTone();
            mState = DOWN_STATE;
            mLastStateChangeTime = nowTime;

        } else if (mState.equals(DOWN_STATE) && pitchDelta < DEG_ERROR) {
            Log.d(TAG, "CHANGE TO UP STATE");
            Log.d(TAG, "pitch=" + pitch + " pitchDelta=" + pitchDelta);
            playTone();
            long stateChangeDeltaTime = nowTime - mLastStateChangeTime;

            // Up movt times are odds
            mStateChangeTimeIntervals.add(stateChangeDeltaTime);
            mState = UP_STATE;
            mLastStateChangeTime = nowTime;

        }
    }

    public int[] convertIntegers(ArrayList<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i);
        }
        return ret;
    }

    public float[] convertFloats(ArrayList<Float> floats)
    {
        float[] ret = new float[floats.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = floats.get(i);
        }
        return ret;
    }

    void detectEndExercise(float pitch, long nowTime) {
        if (Math.abs(pitch - lastBigMovtDeg) > DEG_ERROR) {
            timeOfLastMovt = nowTime;
            lastBigMovtDeg = pitch;
            //Log.d(TAG, "big movt");
        }

        if (nowTime - timeOfLastMovt >= END_TIME_DETECT_THRESHOLD) {
            mState = END_STATE;
        }
    }

    long getEvenIndexAvg(ArrayList<Long> arrayList) {
        int sum = 0;
        int count = 0;

        for (int i = 0; i < arrayList.size(); i+=2) {
            sum += arrayList.get(i);
            count++;
        }

        if (count == 0) {
            return 0;
        } else {
            return Math.round(sum/count);
        }
     }

    long getOddIndexAvg(ArrayList<Long> arrayList) {
        int sum = 0;
        int count = 0;

        for (int i = 1; i < arrayList.size(); i+=2) {
            sum += arrayList.get(i);
            count++;
        }

        if (count == 0) {
            return 0;
        } else {
            return Math.round(sum/count);
        }
    }

    private void playTone() {
        Log.d(TAG, "playTone");
        mediaPlayer = MediaPlayer.create(this, R.raw.ding);
        mediaPlayer.start();
    }
}
