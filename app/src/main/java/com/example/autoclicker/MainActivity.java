package com.example.autoclicker;

import static com.example.autoclicker.Constants.INTENT_FILTER_RECORDED_PLAYS;
import static com.example.autoclicker.Constants.INTENT_PARAM_ACTION;
import static com.example.autoclicker.Constants.INTENT_PARAM_PLAYS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import com.example.autoclicker.db.AppDatabase;
import com.example.autoclicker.db.PlayDao;
import com.example.autoclicker.db.PlaylistDao;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    public static final String DEBUG_TAG = "AUTO_CLICKER_MAIN";

    // db stuff
    private boolean isDbSetup = false;
    private AppDatabase db;
    private PlaylistDao playlistDao;
    private PlayDao playDao;

    private void setupDb() {
        if (!isDbSetup) {
            this.db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "database-name").build();
            this.playlistDao = db.playlistDao();
            this.playDao = db.playDao();
            isDbSetup = true;
        }
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            // We want to receive the list of plays
            ArrayList<Play> recordedPlays = intent.getParcelableArrayListExtra(INTENT_PARAM_PLAYS);
            Log.d(DEBUG_TAG, "onReceive: Broadcast received: " + recordedPlays);

            //Ask user for playlist name (what did you just do?)

            // Store list of plays & name

        }
    };

    protected void onResume(){
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");
//        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter(INTENT_FILTER_RECORDED_PLAYS));
    }

    protected void onPause (){
        super.onPause();
        Log.d(DEBUG_TAG, "onPause");
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate");

        setContentView(R.layout.activity_main);

        findViewById(R.id.startFloat).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter(INTENT_FILTER_RECORDED_PLAYS));
        setupDb();
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

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
        super.onDestroy();
    }
}
