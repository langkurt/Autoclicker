package com.example.autoclicker;

import static com.example.autoclicker.Constants.INTENT_PARAM_ACTION;
import static com.example.autoclicker.Constants.INTENT_PARAM_PLAYS;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.ArrayList;

public class AutoService extends AccessibilityService {


    private final int CLICK_DURATION = 1;
    public static final String DEBUG_TAG = "AUTO_CLICKER_AutoService";
    private Handler mHandler;
    private int mX;
    private int mY;

    private ArrayList<Play> plays;
    private ArrayList<Play> recordedPlays;

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

        if (intent != null) {
            String action = intent.getStringExtra(INTENT_PARAM_ACTION);
            if (action.equals(Action.PLAY.toString())) {
                this.plays = intent.getParcelableArrayListExtra(INTENT_PARAM_PLAYS);
                Log.d(DEBUG_TAG, "AutoService will play: " + this.plays);

                // play the first one, then let the gesture callback play the remaining.
                Play firstPlay = getPlay();
                mX = firstPlay.x();
                mY = firstPlay.y();
                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                mHandler.postDelayed(mRunnable, firstPlay.delay());
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();

            } else if (action.equals(Action.RECORD.toString())) {
                Log.d(DEBUG_TAG, "AutoService will record");

                Toast.makeText(getBaseContext(), "Recording...", Toast.LENGTH_SHORT).show();
            } else if (action.equals(Action.STOP.toString())) {
                Log.d(DEBUG_TAG, "AutoService will stop");
                mHandler.removeCallbacksAndMessages(null);
                // TODO: return recorded plays...
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void playTap(int x, int y) {
        Log.d(DEBUG_TAG, String.format("Playtap at x: %d, y: %d", x, y));
        Path swipePath = new Path();
        swipePath.moveTo(x - 10, y - 10);
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(swipePath, 0, CLICK_DURATION))
                .build();

        boolean isGestureDispatched = this.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(DEBUG_TAG, "playTap -- Gesture Completed");
                super.onCompleted(gestureDescription);
                Play play = getPlay();
                mX = play.x();
                mY = play.y();
                mHandler.postDelayed(mRunnable, play.delay());
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(DEBUG_TAG, "playTap -- Gesture Cancelled");
                super.onCancelled(gestureDescription);
            }
        }, null);

        Log.d(DEBUG_TAG, "Gesture dispatched? " + isGestureDispatched);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(DEBUG_TAG, String.format("SERVICE AccessibilityEvent %s", event.toString()));
    }


    @Override
    public void onInterrupt() {
    }

    private Play getPlay() {
        try {
            return this.plays.remove(0);
        } catch (IndexOutOfBoundsException e) {
            stopSelf();
            return new Play(0, 0, 0);
        }
    }

    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            playTap(mX, mY);
        }
    }
}