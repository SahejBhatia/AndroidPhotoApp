package com.example.sabhatia.assignment2;

import android.content.Context;
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
import android.support.annotation.Nullable;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class canvasView extends View implements View.OnClickListener {

    private Paint mPaint = new Paint();
    private String mCurrentPhotoPath;
    int oX = 200;
    int oY = 200;
    private GestureLibrary mLibrary;
    private static Tracker tracker;


    public canvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);

        mLibrary = GestureLibraries.fromRawResource(getContext(), R.raw.gestures);
        if (!mLibrary.load()) {
            //finish();
        }
    }

    @Override
    protected void onDraw(Canvas canvas){

        super.onDraw(canvas);
        MyApp myApp = (MyApp)getContext().getApplicationContext();
        tracker = myApp.getDefaultTracker();
        Log.i("SCREEN NAME", "Setting screen name: " + this.getClass().getSimpleName());
        tracker.setScreenName("Image~" + this.getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());


        //mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setColor(Color.MAGENTA);

        //canvas.drawColor(Color.WHITE);
        //canvas.drawCircle(oX, oY, 50, mPaint);

        Bitmap btm = BitmapFactory.decodeFile(mCurrentPhotoPath);
        canvas.drawBitmap(btm,20,20,null);

        //GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        //gestures.addOnGesturePerformedListener(this);
    }


    public void onClick(View view){
        oX=oX+10;
        view.invalidate();
    }
    public void setCoordinates(int x, int y) {
        this.oX = x;
        this.oY = y;
    }
    public boolean onKeyDown(int keuCode, KeyEvent event){
        oX=oX+10;
        this.invalidate();

        //invalidate will draw again
        return true;
    }

    public void setmCurrentPhotoPath(String cp){
        this.mCurrentPhotoPath = cp;

    }

    public void reset(){
        invalidate();
    }


}
