package com.example.autoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;


public class AutoService extends AccessibilityService {

    public static final String DEBUG_TAG = "AUTO_CLICKER_AutoService";
    private Handler mHandler;
    private int mX;
    private int mY;

    @Override
    public void onCreate() {

        Log.d(DEBUG_TAG, "SERVICE onCreate");
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }



    @Override
    protected void onServiceConnected() {
        Log.d(DEBUG_TAG, "SERVICE Connected");
        super.onServiceConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "SERVICE STARTED");

        if(intent!=null){
            String action = intent.getStringExtra("action");
            if (action.equals("play")) {
                mX = intent.getIntExtra("x", 0);
                //Log.d("x_value",Integer.toString(mX));
                mY = intent.getIntExtra("y", 0);
                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                //playTap(mX,mY);
                //mHandler.postDelayed(mRunnable, 1000);
                mHandler.post(mRunnable);
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals("stop")){
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }



    // (x, y) in screen coordinates
    private static GestureDescription createClick(float x, float y) {
        // for a single tap a duration of 1 ms is enough
        final int DURATION = 1;

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    private void playTap(int x, int y) {
        Log.d(DEBUG_TAG, String.format("Playtap at x: %d, y: %d", x, y));
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        swipePath.lineTo(x+100, y+100);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 100, 5000));

        Log.d(DEBUG_TAG, getWindows().toString());
        boolean isGestureDispatched = this.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(DEBUG_TAG,"playTap -- Gesture Completed");
                super.onCompleted(gestureDescription);
                //mHandler.postDelayed(mRunnable, 1);
                mHandler.post(mRunnable);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(DEBUG_TAG,"playTap -- Gesture Cancelled");
                super.onCancelled(gestureDescription);
            }
        }, null);

        Log.d(DEBUG_TAG, "Gesture dispatched? "+ isGestureDispatched);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(DEBUG_TAG, String.format("SERVICE AccessibilityEvent %s", event.toString()));
    }


    @Override
    public void onInterrupt() {
    }


    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            //Log.d("clicked","click");
            playTap(mX, mY);
        }
    }
}