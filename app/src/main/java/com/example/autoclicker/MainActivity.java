package com.example.autoclicker;

import static com.example.autoclicker.Constants.INTENT_PARAM_ACTION;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    public static final String DEBUG_TAG = "AUTO_CLICKER_MAIN";
//    public AutoService autoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.startFloat).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);

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

            // Start floating view
            Intent floatingView = new Intent(MainActivity.this, FloatingView.class);

            if (v.getId() == R.id.record) {
                floatingView.putExtra(INTENT_PARAM_ACTION, Action.RECORD.toString());
            } else if (v.getId() == R.id.startFloat) {
                // Should eventually change to passing the saved plays, or maybe an ID to fetch
                // them from storage
                floatingView.putExtra(INTENT_PARAM_ACTION, Action.PLAY.toString());
            }
            startService(floatingView);

            Log.d(DEBUG_TAG, "Handling MainActivity onclick -- finishing");
            finish();
        } else {
            askPermission();
            Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }
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
