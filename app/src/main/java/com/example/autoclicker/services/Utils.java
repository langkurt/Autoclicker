package com.example.autoclicker.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;

public class Utils {

    public static void playTap(float x, float y, AccessibilityService service, Runnable callback) {
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        GestureDescription gestureDescription = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1)).build();

        service.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);

                callback.run();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }
}
