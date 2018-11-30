package com.example.sabhatia.assignment2;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


public class CanvasActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener, SensorEventListener {

    static private String mCurrentPhotoPath;
    private canvasView canvasview;
    private GestureLibrary mLibrary;
    static private int currentPhotoIndex;
    static private int size;
    private ArrayList<String> photoGallery;
    private ArrayList<String> voiceData;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z, deltaX,deltaY,deltaZ;
    private static final int SHAKE_THRESHOLD = 600;
    private static Tracker tracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        MyApp myApp = (MyApp)getApplication();
        tracker = myApp.getDefaultTracker();
        Log.i("SCREEN NAME", "Setting screen name: " + this.getLocalClassName());
        tracker.setScreenName("Activity~" + this.getLocalClassName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        photoGallery = new ArrayList<String>();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer =sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        last_x =0;
        last_y =0;
        last_z =0;

        Bundle extras = getIntent().getExtras();
        mCurrentPhotoPath = extras.getString("path");
        currentPhotoIndex = extras.getInt("index");

        System.out.println("current index  +" + currentPhotoIndex);

        size = extras.getInt("size");
        populateGallery();

        //ImageView iv = (ImageView) findViewById(R.id.imageView2);

        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {
            finish();
        }

        canvasview = findViewById(R.id.canvasView);
        canvasview.setmCurrentPhotoPath(mCurrentPhotoPath);
        //setContentView(canvasview);

        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);
        //iv.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));



    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        voiceData  = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        System.out.println("string from command is :" + voiceData);
        //changePic();
    }


    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Gesture performed")
                .build());


        ArrayList<Prediction> predictions = mLibrary.recognize(gesture);


        System.out.println("size of predictions " + predictions.size());
        // We want at least one prediction
        if (predictions.size() > 0) {
            System.out.println("in size > 0");
            Prediction prediction = predictions.get(0);
            // We want at least some confidence in the result


            if (prediction.name.equalsIgnoreCase("one")) {
                Toast.makeText(this, "one", Toast.LENGTH_SHORT).show();
                ++currentPhotoIndex;
                System.out.println("new index  in one "+ currentPhotoIndex );
                changePic();
            }

            System.out.println(prediction);

            if (prediction.name.equalsIgnoreCase("zero")) {
                System.out.println("zero - swipe left");
                --currentPhotoIndex;

                changePic();
            }

            if (prediction.name.equalsIgnoreCase("two")) {
                System.out.println("go back");
                finish();
            }
        }

    }

    public void changePic() {

        if (currentPhotoIndex < 0)
            currentPhotoIndex = size -1 ;
        if (currentPhotoIndex >= size)
            currentPhotoIndex = 0;


        System.out.println("old path " + mCurrentPhotoPath);
        mCurrentPhotoPath = photoGallery.get(currentPhotoIndex);

        System.out.println("index " + currentPhotoIndex);
        System.out.println(" new path " + mCurrentPhotoPath);

        canvasview.setmCurrentPhotoPath(mCurrentPhotoPath);

        canvasview.reset();


    }


    private ArrayList<String> populateGallery() {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.sabhatia.assignment2/files/Pictures");

        File[] fList = file.listFiles();
        if (fList != null) {
            for (File f : file.listFiles()) {
                photoGallery.add(f.getPath());
            }
        }
        return photoGallery;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {


            deltaX = last_x - sensorEvent.values[0];
         //   deltaY = Math.abs(last_y - sensorEvent.values[1]);
           // deltaZ = Math.abs(last_z - sensorEvent.values[2]);


            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 3000) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;


                if(deltaX <1){
                    Toast.makeText(this, "delatx greater than 0 - swipe right " + deltaX, Toast.LENGTH_SHORT).show();
                    System.out.println("once - swipe right " + currentPhotoIndex);
                    ++currentPhotoIndex;
                    changePic();

                }else if (deltaX > 1){
                    Toast.makeText(this, "deltax is less ta=han 0 - swipe left"+ deltaX, Toast.LENGTH_SHORT).show();
                    System.out.println("zero - swipe left");
                    --currentPhotoIndex;
                    changePic();
                }


                last_x = sensorEvent.values[0];
                //last_y = sensorEvent.values[1];
                //last_z = sensorEvent.values[2];
            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
