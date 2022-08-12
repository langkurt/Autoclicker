package com.example.autoclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    public static final String DEBUG_TAG = "AUTO_CLICKER_MAIN";
//    public AutoService autoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        autoService = new AutoService();


        findViewById(R.id.startFloat).setOnClickListener(this);

    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    @Override
    public void onClick(View v) {
        if (Settings.canDrawOverlays(this)) {
            Log.d(DEBUG_TAG, "Handling MainActivity onclick -- starting FloatingView service");
            startService(new Intent(MainActivity.this, FloatingView.class));
            Log.d(DEBUG_TAG, "Handling MainActivity onclick -- finishing");
            finish();
        } else {
            askPermission();
            Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean handleEvent(MotionEvent event) {

        int x = (int)event.getX();
        int y = (int)event.getY();

        Intent intent = new Intent(MainActivity.this, AutoService.class);
        intent.putExtra("x", x);
        intent.putExtra("y", y);

        Log.d(DEBUG_TAG, String.format("click at x: %d; y: %d", x, y));
        startService(intent);

        return false;
    }


    /*
    * boiler
    *
    * */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
