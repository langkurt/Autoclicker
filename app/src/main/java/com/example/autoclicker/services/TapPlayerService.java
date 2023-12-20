package com.example.autoclicker.services;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.autoclicker.shared.Play;

import java.util.ArrayList;

public class TapPlayerService {

    private static final String DEBUG_TAG = TapPlayerService.class.getSimpleName();
    private final AutoService autoService;
    private final Handler mHandler;
    private IntervalRunnable mRunnable;
    private float mX;
    private float mY;
    private ArrayList<Play> plays;

    public TapPlayerService(AutoService autoService) {

        Log.d(DEBUG_TAG, "TapPlayerService constructor.");
        this.autoService = autoService;

        HandlerThread handlerThread = new HandlerThread("tap-player-handler");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper());
    }

    public void startPlayer(ArrayList<Play> plays) {
        Log.d(DEBUG_TAG, "Player Started with plays: " + plays);

        // play the first one, then let the gesture callback play the remaining.
        this.plays = plays;
        Play firstPlay = getNextPlay();
        mX = firstPlay.x();
        mY = firstPlay.y();
        if (mRunnable == null) {
            mRunnable = new IntervalRunnable();
        }
        mHandler.postDelayed(mRunnable, (long) firstPlay.delay());
    }

    public void stopPlayer() {
        Log.d(DEBUG_TAG, "TapPlayer will stop");
        mHandler.removeCallbacksAndMessages(null);
    }

    private Play getNextPlay() {
        return this.plays.remove(0);
    }

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            Utils.playTap(mX, mY, autoService, () -> {
                if (plays != null && !plays.isEmpty()) {
                    Play play = getNextPlay();
                    mX = play.x();
                    mY = play.y();

                    Log.d(DEBUG_TAG, "playing next tap : " + play);
                    mHandler.postDelayed(mRunnable, (long) play.delay());
                }
            });
        }
    }
}