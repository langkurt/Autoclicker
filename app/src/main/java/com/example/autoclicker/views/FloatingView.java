package com.example.autoclicker.views;

import static com.example.autoclicker.shared.Constants.INTENT_PARAM_ACTION;
import static com.example.autoclicker.shared.Constants.INTENT_PARAM_PLAYS;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.autoclicker.shared.Action;
import com.example.autoclicker.MainActivity;
import com.example.autoclicker.shared.Play;
import com.example.autoclicker.R;
import com.example.autoclicker.services.AutoService;

import java.util.ArrayList;
import java.util.Objects;

public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;
    private WindowManager.LayoutParams myFloatingViewLayoutParams;
    public static final String DEBUG_TAG = "AUTO_CLICKER_FLOATING_VIEW";

    private Intent startingIntent;

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
        Log.d(DEBUG_TAG, "onStartCommand with intent: " + intent.getExtras());

        // Save the intent for later
        this.startingIntent = intent;
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
            intent.putExtra(INTENT_PARAM_ACTION, this.startingIntent.getStringExtra(INTENT_PARAM_ACTION));

            Log.d(DEBUG_TAG, "onClick: received the following intent " + this.startingIntent);
            Log.d(DEBUG_TAG, "onClick: received the following intent Extras " + this.startingIntent.getExtras());

            if (Objects.equals(this.startingIntent.getStringExtra(INTENT_PARAM_ACTION), Action.PLAY.toString())) {
                ArrayList<Play> plays = this.startingIntent.getParcelableArrayListExtra(INTENT_PARAM_PLAYS);

                Log.d(DEBUG_TAG, "onClick: received the following plays " + plays);
                if (plays == null) {
                    Log.d(DEBUG_TAG, "onClick: Plays received from the intent are null. Returning");
                    return;
                }
                intent.putExtra(INTENT_PARAM_PLAYS, plays);
            }

            getApplication().startService(intent);

        } else if (v.getId() == R.id.stop) {
            Log.d(DEBUG_TAG, "STOP was clicked from the floating view");
            mWindowManager.removeView(myFloatingView);

            // Start the main activity
            Intent appMain = new Intent(getApplicationContext(), MainActivity.class);
            appMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(appMain);

            // Stop the autoclicker service
            Intent intent = new Intent(getApplicationContext(), AutoService.class);
            intent.putExtra(INTENT_PARAM_ACTION, Action.STOP.toString());
            getApplication().startService(intent);


        }
    }

    private void displayFloatingView() {

        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        //setting the layout parameters
        myFloatingViewLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
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
