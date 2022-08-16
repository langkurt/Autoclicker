package com.example.autoclicker;

import static com.example.autoclicker.Constants.INTENT_PARAM_ACTION;
import static com.example.autoclicker.Constants.INTENT_PARAM_PLAYS;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;
    private WindowManager.LayoutParams myFloatingViewLayoutParams;
    public static final String DEBUG_TAG = "AUTO_CLICKER_FLOATING_VIEW";

    private Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG_TAG, "onBind");
        return null;
    }


    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate();
        displayFloatingView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "onStartCommand with intent: " + intent);

        // Save the intent for later
        this.intent = intent;
        try {
            mWindowManager.addView(myFloatingView, myFloatingViewLayoutParams);
        } catch (IllegalStateException e) {
            Log.d(DEBUG_TAG, "FAB view was already added, pass");
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        super.onDestroy();
        if (myFloatingView != null) mWindowManager.removeView(myFloatingView);
    }


    @Override
    public void onClick(View v) {
        Log.d(DEBUG_TAG, "onClick");

        //Log.d("onClick","THIS IS CLICKED");
        if (v.getId() == R.id.start) {
            Log.d(DEBUG_TAG, "START was clicked from the floating view");
            // Starting either the recording or a playback
            Intent intent = new Intent(getApplicationContext(), AutoService.class);
            intent.putExtra(INTENT_PARAM_ACTION, this.intent.getStringExtra(INTENT_PARAM_ACTION));

            // Are we trying to playback? pass the plays, but since we dont have any, play against
            // a single point... for now.
            if (Objects.equals(intent.getStringExtra(INTENT_PARAM_ACTION), Action.PLAY.toString())) {
                ArrayList<Play> plays = intent.getParcelableArrayListExtra(INTENT_PARAM_PLAYS);
                if (plays != null) {
                    intent.putExtra(INTENT_PARAM_PLAYS, plays);
                } else {
                    int[] location = new int[2];
                    myFloatingView.getLocationOnScreen(location);
                    intent.putExtra(INTENT_PARAM_PLAYS, new ArrayList<>(
                            Collections.singletonList(new Play(location[0], location[1], 0))));
                }
            }

            getApplication().startService(intent);

        } else if (v.getId() == R.id.stop) {
            Log.d(DEBUG_TAG, "STOP was clicked from the floating view");
            mWindowManager.removeView(myFloatingView);
            Intent appMain = new Intent(getApplicationContext(), MainActivity.class);

            // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag
            appMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            getApplication().startActivity(appMain);
        }
    }

    private void displayFloatingView() {


        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);


        int layout_parms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //setting the layout parameters
        myFloatingViewLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        // getting windows services and adding the floating view to it
        Log.d(DEBUG_TAG, "displayFloatingView -- getting windows services and adding the floating view to it");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, myFloatingViewLayoutParams);

        // adding a touchListener to make drag movement of the floating widget
        Log.d(DEBUG_TAG, "displayFloatingView -- adding an touchlistener to make drag movement of the floating widget");
        myFloatingView.findViewById(R.id.thisIsAnID).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(DEBUG_TAG, "THIS IS TOUCHED");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = myFloatingViewLayoutParams.x;
                        initialY = myFloatingViewLayoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        myFloatingViewLayoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        myFloatingViewLayoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, myFloatingViewLayoutParams);
                        return true;
                }
                return false;
            }
        });

        Button startButton = (Button) myFloatingView.findViewById(R.id.start);
        startButton.setOnClickListener(this);
        Button stopButton = (Button) myFloatingView.findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
    }
}
