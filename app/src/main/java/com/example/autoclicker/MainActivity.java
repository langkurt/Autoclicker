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
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import com.example.autoclicker.adapters.PlaylistAdapter;
import com.example.autoclicker.db.AppDatabase;
import com.example.autoclicker.db.PlayDao;
import com.example.autoclicker.db.PlayItem;
import com.example.autoclicker.db.Playlist;
import com.example.autoclicker.db.PlaylistDao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    public static final String DEBUG_TAG = "AUTO_CLICKER_MAIN";

    private PlaylistAdapter playlistAdapter;

    // db stuff
    private boolean isSetup = false;
    private AppDatabase db;
    private PlaylistDao playlistDao;
    private PlayDao playDao;

    /**
     * Onclick handle for when the play list item 'delete' button is clicked
     * */
    public void deletePlaylistOnClickHandler(View view) {
        int playlistId = (int) view.getTag();
        Log.d(DEBUG_TAG, "deletePlaylistOnClickHandler: Deleting Plays and Playlist with PlaylistId: " + playlistId);
        playlistDao.deleteById(playlistId);
        playDao.deleteByPlaylistId(playlistId);

        playlistAdapter.update(playlistDao.getAll());
    }

    /**
     * Onclick handle for when the play list item 'play' button is clicked
     * */
    public void startPlaylistOnClickHandler(View view) {
        Log.d(DEBUG_TAG, "startPlaylistOnClickHandler: playlist id to fetch " + view.getTag());

        Playlist playlist = playlistDao.getOne((int) view.getTag());
        List<PlayItem> playItems = playDao.getAllFromPlaylist(playlist.playlistId);

        Log.d(DEBUG_TAG, "playlist: " + playlist);
        Log.d(DEBUG_TAG, "play items: " + playItems.toString());

        if (playItems.isEmpty()) {
            Log.d(DEBUG_TAG, "Playlist is empty, returning");
            return;
        }
        Log.d(DEBUG_TAG, "plays from db: " + playItems);
        List<Play> playsToSend = playItems.stream().map(playItem ->
                new Play(playItem.xCoordinate, playItem.yCoordinate, playItem.delay)
        ).collect(Collectors.toList());
        Log.d(DEBUG_TAG, String.format("%d plays to send: %s", playsToSend.size(), playsToSend));

        // Start floating view
        Log.d(DEBUG_TAG, "startPlaylistOnClickHandler -- starting FloatingView service");
        Intent floatingView = new Intent(MainActivity.this, FloatingView.class);
        floatingView.putExtra(INTENT_PARAM_ACTION, Action.PLAY.toString());
        floatingView.putExtra(INTENT_PARAM_PLAYS, new ArrayList<Play>(playsToSend));
        startService(floatingView);
    }

    public void describePlaylistOnClickHandler(View view) {
        Log.d(DEBUG_TAG, "describePlaylistOnClickHandler: playlist id to fetch " + view.getTag());

        Playlist playlist = playlistDao.getOne((int) view.getTag());
        Log.d(DEBUG_TAG, "playlist: " + playlist);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate with this " + this);
        if (savedInstanceState != null) {
            boolean isffv = savedInstanceState.getBoolean("isFromFloatingView");
            Log.d(DEBUG_TAG, "onCreate: isffv: " + isffv);

        }

        setContentView(R.layout.activity_main);
        findViewById(R.id.record).setOnClickListener(this);

        // one time setup
        setup();

        // Uncomment and run once to start fresh...
//         db.clearAllTables();
        createPlaylistUI();
        Log.d(DEBUG_TAG, "onCreate: Database has.. " + playlistDao.getPlaylistsWithPlays());
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    private void createPlaylistUI() {
        ListView playlistListView = (ListView) findViewById(R.id.playlist_list_view);
        playlistAdapter = new PlaylistAdapter(this, playlistDao.getAll());
        playlistListView.setAdapter(playlistAdapter);

    }

    // Broadcast receiver for listening to when the autoservice returns recorded clicks
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // We want to receive the list of plays
            ArrayList<Play> recordedPlays = intent.getParcelableArrayListExtra(INTENT_PARAM_PLAYS);
            Log.d(DEBUG_TAG, "onReceive: Broadcast received: " + recordedPlays);

            // todo: Ask user for playlist name (what did you just do?)
            String name = String.format("%s taps", recordedPlays.size());

            storeRecordedPlays(name, recordedPlays);

        }
    };

    private void storeRecordedPlays(String name, List<Play> recordedPlays) {
        if (recordedPlays.isEmpty()) {
            Log.d(DEBUG_TAG, "storeRecordedPlays: Nothing to store");
            return;
        }
        // Store list of plays & name
        Playlist playlist = new Playlist();
        playlist.name = name;
        Log.d(DEBUG_TAG, "storeRecordedPlays: inserting playlist ");
        long playlistId = playlistDao.insert(playlist);

        for (Play play : recordedPlays) {
            PlayItem playItem = new PlayItem();
            playItem.playListId = playlistId;
            playItem.xCoordinate = play.x();
            playItem.yCoordinate = play.y();
            playItem.delay = play.delay();
            Log.d(DEBUG_TAG, "storeRecordedPlays: inserting playItem " + playItem);
            playDao.insert(playItem);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(DEBUG_TAG, "onClick: view is " + v);
        if (Settings.canDrawOverlays(this)) {

            if (v.getId() == R.id.record) {
                Log.d(DEBUG_TAG, "Handling MainActivity onclick -- starting FloatingView service");

                // Start floating view
                Intent floatingView = new Intent(MainActivity.this, FloatingView.class);
                floatingView.putExtra(INTENT_PARAM_ACTION, Action.RECORD.toString());
                startService(floatingView);

//                Log.d(DEBUG_TAG, "Handling MainActivity onclick -- finishing");
//                finish();

            }

        } else {
            askPermission();
            Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }
    }

    private void setup() {
        if (!isSetup) {
            Log.d(DEBUG_TAG, "setup: Setting up db and broadcast receiver");

            LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
            LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter(INTENT_FILTER_RECORDED_PLAYS));

            this.db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                    .allowMainThreadQueries()
                    .build();
            this.playlistDao = db.playlistDao();
            this.playDao = db.playDao();
            isSetup = true;
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
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume");
        playlistAdapter.update(playlistDao.getAll());
//        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter(INTENT_FILTER_RECORDED_PLAYS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause");
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }
}
