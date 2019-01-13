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
    private static final String PITCH_DATA_KEY = "PITCH_DATA_KEY";
    private static final String TIME_DATA_KEY = "TIME_DATA_KEY";
    private static final String TAG = "WL/ReviewActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Bundle intentBundle = getIntent().getExtras();

        if (intentBundle != null) {
            pitchDataArr = intentBundle.getFloatArray(PITCH_DATA_KEY);
            timeDataArr = intentBundle.getIntArray(TIME_DATA_KEY);

            Log.d(TAG, Arrays.toString(pitchDataArr));
            Log.d(TAG, Arrays.toString(timeDataArr));
        }
        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }

}
