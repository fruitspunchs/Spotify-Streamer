package com.example.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/*
 * Holds PlayerFragment in phone layout.
 */
public class PlayerActivity extends AppCompatActivity {
    private static String LOG_TAG;
    private static String PLAYER_FRAGMENT_TAG = "playerFragment";
    private MenuItem mShareItem;
    private ShareActionProvider mShareActionProvider;

    /*
     * Listens for MediaPlayerService broadcasts.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MediaPlayerService.MEDIA_EVENT_KEY);
            if (!message.equals(MediaPlayerService.MEDIA_TRACK_PROGRESS)) {
                Log.d(LOG_TAG, "Got message: " + message);
            }

            switch (message) {
                case MediaPlayerService.MEDIA_REPLY_TRACK_LOADED:
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(Utility.createShareTrackIntent(intent.getStringExtra(MediaPlayerService.TRACK_URL_KEY)));
                    }
                    if (mShareItem != null) {
                        mShareItem.setVisible(true);
                    }
                    break;
                case MediaPlayerService.MEDIA_REPLY_TRACK_NOT_LOADED:
                    if (mShareItem != null) {
                        mShareItem.setVisible(false);
                    }
                    break;
            }
        }
    };

    /*
     * Creates a PlayerFragment and passes intent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOG_TAG = this.getClass().getSimpleName();

        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        String artistName = intent.getStringExtra(PlayerFragment.ARTIST_NAME_KEY);
        TrackInfo trackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
        int trackPosition = intent.getIntExtra(PlayerFragment.TRACK_INDEX_KEY, 0);
        int bufferProgress = intent.getIntExtra(MediaPlayerService.BUFFER_PROGRESS_KEY, 0);
        boolean isPlaying = intent.getBooleanExtra(MediaPlayerService.IS_PLAYING_KEY, false);
        int seekBarPosition = intent.getIntExtra(MediaPlayerService.TRACK_PROGRESS_KEY, 0);

        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(PlayerFragment.ARTIST_NAME_KEY, artistName);
        args.putParcelable(PlayerFragment.TRACK_INFO_KEY, trackInfo);
        args.putInt(PlayerFragment.TRACK_INDEX_KEY, trackPosition);
        args.putInt(MediaPlayerService.BUFFER_PROGRESS_KEY, bufferProgress);
        args.putBoolean(MediaPlayerService.IS_PLAYING_KEY, isPlaying);
        args.putInt(MediaPlayerService.TRACK_PROGRESS_KEY, seekBarPosition);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment_container, fragment, PLAYER_FRAGMENT_TAG).commit();
    }

    /*
     * Registers for MediaPlayerService broadcasts and requests if a track is loaded.
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(MediaPlayerService.MEDIA_EVENT));
        Intent requestServiceIsTrackLoaded = new Intent(this, MediaPlayerService.class).setAction(MediaPlayerService.MEDIA_REQUEST_IS_TRACK_LOADED);
        startService(requestServiceIsTrackLoaded);
    }

    /*
     * Unregisters for broadcasts.
     */
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    /*
     * Adds action bar items.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        mShareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
        return true;
    }

    /*
     * Detects if an action bar item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
