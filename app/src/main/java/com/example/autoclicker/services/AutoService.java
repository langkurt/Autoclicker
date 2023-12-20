package com.example.autoclicker.services;

import static com.example.autoclicker.shared.Constants.INTENT_PARAM_ACTION;
import static com.example.autoclicker.shared.Constants.INTENT_PARAM_PLAYS;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.example.autoclicker.shared.Action;
import com.example.autoclicker.shared.Play;

import java.util.ArrayList;
import java.util.Objects;

public class AutoService extends AccessibilityService {
    public static final String DEBUG_TAG = AutoService.class.getSimpleName();

    private TapRecorderService tapRecorderService;
    private TapPlayerService tapPlayerService;

    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "onCreate");
        this.tapRecorderService = new TapRecorderService(this);
        this.tapPlayerService = new TapPlayerService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null)
            return super.onStartCommand(intent, flags, startId);

        String receivedAction = intent.getStringExtra(INTENT_PARAM_ACTION);
        Log.d(DEBUG_TAG, "onStartCommand: Recieved action is: " + receivedAction);

        // Start Playing
        if (Objects.equals(receivedAction, Action.PLAY.toString())) {
            ArrayList<Play> plays = intent.getParcelableArrayListExtra(INTENT_PARAM_PLAYS);
            this.tapPlayerService.startPlayer(plays);
            Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
        }

        // Start Recording
        if (Objects.equals(receivedAction, Action.RECORD.toString())) {
            this.tapRecorderService.startRecording();
            Toast.makeText(getBaseContext(), "Recording...", Toast.LENGTH_SHORT).show();
        }

        // Stop playing and recording services
        if (Objects.equals(receivedAction, Action.STOP.toString())) {
            this.tapRecorderService.stopRecording();
            this.tapPlayerService.stopPlayer();
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");

        this.tapRecorderService.stopRecording();
        this.tapPlayerService.stopPlayer();

        super.onDestroy();
    }


    @Override
    protected void onServiceConnected() {
        Log.d(DEBUG_TAG, "SERVICE Connected");
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}