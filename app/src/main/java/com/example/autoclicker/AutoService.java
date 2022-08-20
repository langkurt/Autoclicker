package com.example.autoclicker;

import static com.example.autoclicker.Constants.INTENT_PARAM_ACTION;
import static com.example.autoclicker.Constants.INTENT_PARAM_PLAYS;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class AutoService extends AccessibilityService implements View.OnTouchListener {


    private final int CLICK_DURATION = 1;
    public static final String DEBUG_TAG = "AUTO_CLICKER_AutoService";
    private long previousEventTime;

    private Handler mHandler;
    private WindowManager mWindowManager;
    private LinearLayout touchLayout;      // linear layout will use to detect touch event
    private WindowManager.LayoutParams touchLayoutParams;
    private WindowManager.LayoutParams resizedTouchLayoutParams;
    private float mX;
    private float mY;


    private ArrayList<Play> plays;
    private ArrayList<Play> recordedPlays;

    @Override
    public void onCreate() {

        Log.d(DEBUG_TAG, "SERVICE onCreate");
        super.onCreate();
        recordedPlays = new ArrayList<>();
        previousEventTime = 0;

        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        // view layout stuff to attach a touch listener to
        touchLayout = new LinearLayout(this);
        // set layout width 30 px and height is equal to full screen
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(30, WindowManager.LayoutParams.MATCH_PARENT);
        touchLayout.setLayoutParams(lp);
        // set color if you want layout visible on screen
//        touchLayout.setBackgroundColor(Color.CYAN);
        // set on touch listener
        touchLayout.setOnTouchListener(this);


        // fetch window manager object
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // set layout parameter of window manager
        touchLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, // width of layout 30 px
                WindowManager.LayoutParams.MATCH_PARENT, // height is equal to full screen
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        touchLayoutParams.gravity = Gravity.START | Gravity.TOP;

        // Used to move the touch layout out of the way when passing the captured tap to underlying window
        resizedTouchLayoutParams = new WindowManager.LayoutParams(
                1, // width of 1 px
                1, // height of 1px
                touchLayoutParams.type,
                touchLayoutParams.flags,
                touchLayoutParams.format);
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
                Play firstPlay = getNextPlay();
                mX = firstPlay.x();
                mY = firstPlay.y();
                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                mHandler.postDelayed(mRunnable, (long) firstPlay.delay());
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();

            } else if (action.equals(Action.RECORD.toString())) {
                Log.d(DEBUG_TAG, "AutoService will record");
                addTouchLayoutView();
                Toast.makeText(getBaseContext(), "Recording...", Toast.LENGTH_SHORT).show();
            } else if (action.equals(Action.STOP.toString())) {
                Log.d(DEBUG_TAG, "AutoService will stop");
                if (touchLayout != null) mWindowManager.removeView(touchLayout);
                mHandler.removeCallbacksAndMessages(null);
                // TODO: return recorded plays...
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        super.onDestroy();
        if (touchLayout != null) mWindowManager.removeView(touchLayout);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(DEBUG_TAG, String.format("SERVICE AccessibilityEvent %s", event.toString()));
    }


    @Override
    public void onInterrupt() {
        Log.d(DEBUG_TAG, "onInterrupt");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(DEBUG_TAG, "Touch Detected \tAction :" + event.getAction() +
                    "\t X :" + event.getRawX() +
                    "\t Y :" + event.getRawY() +
                    "\t Time: " + event.getEventTime());
            Log.d(DEBUG_TAG, event.toString());

            //  Store the tap
            addTap(event.getRawX(), event.getRawY(), event.getEventTime());

            mWindowManager.updateViewLayout(touchLayout, resizedTouchLayoutParams);
            playTap(event.getRawX(), event.getRawY());
            return true;
        }
        return false; // True if the listener has consumed the event, false otherwise

    }

    private void addTap(float x, float y, long eventTime) {
        long delay = previousEventTime == 0 ? 0 : eventTime - previousEventTime;
        previousEventTime = eventTime;
        Play play = new Play(x, y, delay);
        Log.d(DEBUG_TAG, "addTap: adding play: " + play);
        recordedPlays.add(new Play(x, y, delay));
        Log.d(DEBUG_TAG, "addTap: recordedPlays: " + recordedPlays);
    }

    private void playTap(float x, float y) {
        Log.d(DEBUG_TAG, String.format("Playtap at x: %f, y: %f", x, y));
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(swipePath, 0, CLICK_DURATION))
                .build();

        boolean isGestureDispatched = this.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(DEBUG_TAG, "playTap -- Gesture Completed");
                super.onCompleted(gestureDescription);

                mWindowManager.updateViewLayout(touchLayout, touchLayoutParams); // Should be moved back to the onTouch callback

                if (plays != null && !plays.isEmpty()) {
                    Play play = getNextPlay();
                    mX = play.x();
                    mY = play.y();
                    mHandler.postDelayed(mRunnable, (long) play.delay());
                } else {
                    // we're done here.
                    stopSelf(); // TODO: this needed?
//                    disableSelf();
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(DEBUG_TAG, "playTap -- Gesture Cancelled");
                super.onCancelled(gestureDescription);
            }
        }, null);

        Log.d(DEBUG_TAG, "Gesture dispatched? " + isGestureDispatched);
    }

    private Play getNextPlay() {
        Play play = this.plays.remove(0);
        Log.d(DEBUG_TAG, "getPlay: returning play " + play);
        return play;
    }

    private void addTouchLayoutView() {
        try {
            Log.d(DEBUG_TAG, "adding View");
            mWindowManager.addView(touchLayout, touchLayoutParams);
        } catch (IllegalStateException e) {
            Log.d(DEBUG_TAG, "Touch view was already added, pass");
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