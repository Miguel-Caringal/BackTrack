package com.example.miguelcaringal.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;

public class ReviewActivity extends AppCompatActivity {

    float[] pitchDataArr;
    int[] timeDataArr;
    int downMovtAvgTime, upMovtAvgTime;
    private static final String PITCH_DATA_KEY = "PITCH_DATA_KEY";
    private static final String TIME_DATA_KEY = "TIME_DATA_KEY";
    private static final String AVG_DOWN_MVNT_TIME_KEY = "AVG_DOWN_MVNT_TIME_KEY";
    private static final String AVG_UP_MVNT_TIME_KEY = "AVG_UP_MVNT_TIME_KEY";
    private static final String TAG = "WL/ReviewActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Bundle intentBundle = getIntent().getExtras();

        if (intentBundle != null) {
            pitchDataArr = intentBundle.getFloatArray(PITCH_DATA_KEY);
            timeDataArr = intentBundle.getIntArray(TIME_DATA_KEY);
            downMovtAvgTime = intentBundle.getInt(AVG_DOWN_MVNT_TIME_KEY);
            upMovtAvgTime = intentBundle.getInt(AVG_UP_MVNT_TIME_KEY);

            Log.d(TAG, Arrays.toString(pitchDataArr));
            Log.d(TAG, Arrays.toString(timeDataArr));
        }

        ArrayList<DataPoint> dataPointArrayList = new ArrayList<>();

        for (int i = 0; i < timeDataArr.length; i++) {
            if (i < pitchDataArr.length) {
                dataPointArrayList.add(new DataPoint(timeDataArr[i]/1000, pitchDataArr[i]));
            }
        }

        DataPoint[] dataPoints = new DataPoint[dataPointArrayList.size()];

        for (int i=0; i < dataPoints.length; i++) {
            dataPoints[i] = dataPointArrayList.get(i);
        }

        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.addSeries(series);
        graph.setTitle("Squat Angle Delta over Time");
    }

}
