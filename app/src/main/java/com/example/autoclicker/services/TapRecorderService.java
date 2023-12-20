package com.example.autoclicker.services;

import static com.example.autoclicker.shared.Constants.INTENT_FILTER_RECORDED_PLAYS;
import static com.example.autoclicker.shared.Constants.INTENT_PARAM_PLAYS;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.autoclicker.shared.Play;

import java.util.ArrayList;

public class TapRecorderService implements View.OnTouchListener {

    private static final String DEBUG_TAG = TapRecorderService.class.getSimpleName();
    private final AutoService autoService;
    private final WindowManager mWindowManager;
    // linear layout will use to detect touch event
    private final LinearLayout touchLayout;
    private final WindowManager.LayoutParams fullScreenTouchLayoutParams;
    private final WindowManager.LayoutParams minimizedTouchLayoutParams;
    private final ArrayList<Play> recordedPlays;
    private long previousEventTime;

    public TapRecorderService(AutoService autoService) {
        Log.d(DEBUG_TAG, "TapRecorderService created");

        this.autoService = autoService;
        recordedPlays = new ArrayList<>();
        previousEventTime = 0;

        // set layout width 30 px and height is equal to full screen
        touchLayout = new LinearLayout(this.autoService);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(30, WindowManager.LayoutParams.MATCH_PARENT);
        touchLayout.setLayoutParams(lp);
        touchLayout.setOnTouchListener(this);

        // fetch window manager object
        mWindowManager = (WindowManager) this.autoService.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        // set layout parameter of window manager
        fullScreenTouchLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, // equal to full screen
                WindowManager.LayoutParams.MATCH_PARENT, // equal to full screen
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // this window won't ever get key input focus
                PixelFormat.TRANSLUCENT);
        fullScreenTouchLayoutParams.gravity = Gravity.START | Gravity.TOP;

        // Used to move the touch layout out of the way when passing the captured tap to underlying window
        minimizedTouchLayoutParams = new WindowManager.LayoutParams(
                1, // width of 1 px
                1, // height of 1px
                fullScreenTouchLayoutParams.type,
                fullScreenTouchLayoutParams.flags,
                fullScreenTouchLayoutParams.format);
    }

    public void startRecording() {
        Log.d(DEBUG_TAG, "Starting recording");

        // reset recording vars for fresh start
        recordedPlays.clear();
        previousEventTime = 0;

        addTouchLayoutView();

    }

    public void stopRecording() {
        Log.d(DEBUG_TAG, "Tap Recorder will stop");
        removeTouchLayoutView();
        sendBroadcast();
        recordedPlays.clear();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(DEBUG_TAG, "Touch Detected \tAction :" + event.getAction() +
                    "\t X :" + event.getRawX() +
                    "\t Y :" + event.getRawY() +
                    "\t Time: " + event.getEventTime());

            //  Store the tap
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            recordTap(x, y, event.getEventTime());

            mWindowManager.updateViewLayout(touchLayout, minimizedTouchLayoutParams);
            Utils.playTap(x, y, this.autoService, () -> mWindowManager.updateViewLayout(touchLayout, fullScreenTouchLayoutParams));
            return true;
        }
        return false; // True if the listener has consumed the event, false otherwise
    }

    private void sendBroadcast() {
        if (recordedPlays.isEmpty()) {
            Log.d(DEBUG_TAG, "sendBroadcast: recorded plays is empty. Not sending broadcast");
            return;
        }

        // Remove the last play which would be the tap for `stop`
        recordedPlays.remove(recordedPlays.size() - 1);

        Intent intent = new Intent(INTENT_FILTER_RECORDED_PLAYS);
        intent.putExtra(INTENT_PARAM_PLAYS, new ArrayList<>(recordedPlays));
        boolean isBroadcastSent = LocalBroadcastManager.getInstance(this.autoService).sendBroadcast(intent);

        Log.d(DEBUG_TAG, String.format("Sending %d recorded plays: %s", recordedPlays.size(), recordedPlays));
        Log.d(DEBUG_TAG, "isBroadcastSent? " + isBroadcastSent);
    }

    private void recordTap(float x, float y, long eventTime) {
        long delay = previousEventTime == 0 ? 0 : eventTime - previousEventTime;

        if (recordedPlays.size() > 0) {
            Play lastPlay = recordedPlays.get(recordedPlays.size() - 1);
            if (x == lastPlay.x() && y == lastPlay.y()) {
                Log.d(DEBUG_TAG, String.format("addTap: Same x: %f, y: %f coordinates as previous tap. Skipping", x, y));
                return;
            }
        }
        if (delay < 100 && delay > 0) {
            // short delay means the tap is replaying by the system. Not a human tap
            Log.d(DEBUG_TAG, String.format("addTap: Short delay of %d. Skipping", delay));
            return;
        }
        previousEventTime = eventTime;
        recordedPlays.add(new Play(x, y, delay));

        Log.d(DEBUG_TAG, "addTap: recordedPlays: " + recordedPlays);
    }

    private void addTouchLayoutView() {
        try {
            Log.d(DEBUG_TAG, "adding View");
            mWindowManager.addView(touchLayout, fullScreenTouchLayoutParams);
        } catch (IllegalStateException e) {
            Log.d(DEBUG_TAG, "Touch view was already added, pass");
        }
    }

    private void removeTouchLayoutView() {
        try {
            if (touchLayout != null) mWindowManager.removeView(touchLayout);
        } catch (IllegalArgumentException e) {
            Log.d(DEBUG_TAG, "removeTouchLayoutView: window not attached to window manager but idgaf");
        }
    }
}