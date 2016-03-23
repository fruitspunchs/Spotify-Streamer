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
 * Holds Top10Tracks fragment in phone layout
 */
public class Top10TracksActivity extends AppCompatActivity implements Top10TracksFragment.ItemSelectedCallback {

    private static String LOG_TAG;
    private boolean mTwoPane;
    private MenuItem mNowPlayingMenuItem;
    private MenuItem mShareItem;
    private ShareActionProvider mShareActionProvider;
    /*
     * Listens for media player service broadcasts
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
                    if (mNowPlayingMenuItem != null) {
                        mNowPlayingMenuItem.setVisible(true);
                    }
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(Utility.createShareTrackIntent(intent.getStringExtra(MediaPlayerService.TRACK_URL_KEY)));
                    }
                    if (mShareItem != null) {
                        mShareItem.setVisible(true);
                    }
                    break;
                case MediaPlayerService.MEDIA_REPLY_TRACK_NOT_LOADED:
                    if (mNowPlayingMenuItem != null) {
                        mNowPlayingMenuItem.setVisible(false);
                    }
                    if (mShareItem != null) {
                        mShareItem.setVisible(false);
                    }
                    break;
                case MediaPlayerService.MEDIA_REPLY_CURRENT_TRACK_STATUS:
                    Intent showPlayerIntent;
                    String artistName = intent.getStringExtra(PlayerFragment.ARTIST_NAME_KEY);
                    TrackInfo trackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
                    int trackPosition = intent.getIntExtra(PlayerFragment.TRACK_INDEX_KEY, 0);
                    int bufferProgress = intent.getIntExtra(MediaPlayerService.BUFFER_PROGRESS_KEY, 0);
                    boolean isPlaying = intent.getBooleanExtra(MediaPlayerService.IS_PLAYING_KEY, false);
                    int seekBarPosition = intent.getIntExtra(MediaPlayerService.TRACK_PROGRESS_KEY, 0);

                    if (!mTwoPane) {
                        showPlayerIntent = new Intent(getApplicationContext(), PlayerActivity.class);
                        showPlayerIntent.putExtra(PlayerFragment.ARTIST_NAME_KEY, artistName)
                                .putExtra(PlayerFragment.TRACK_INFO_KEY, trackInfo)
                                .putExtra(PlayerFragment.TRACK_INDEX_KEY, trackPosition)
                                .putExtra(MediaPlayerService.BUFFER_PROGRESS_KEY, bufferProgress)
                                .putExtra(MediaPlayerService.IS_PLAYING_KEY, isPlaying)
                                .putExtra(MediaPlayerService.TRACK_PROGRESS_KEY, seekBarPosition);

                        showPlayerIntent.setAction(PlayerFragment.ACTION_LAUNCH_ONLY);
                        startActivity(showPlayerIntent);
                    }
                    break;
            }
        }
    };

    /*
     * Add Top10Fragment to view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_tracks);
        LOG_TAG = getClass().getSimpleName();
        mTwoPane = getResources().getBoolean(R.bool.wide_layout);

        if (null == savedInstanceState) {
            Bundle arguments = new Bundle();
            arguments.putString(MainActivity.ARTIST_ID_KEY, getIntent().getStringExtra(MainActivity.ARTIST_ID_KEY));
            arguments.putString(MainActivity.ARTIST_NAME_KEY, getIntent().getStringExtra(MainActivity.ARTIST_NAME_KEY));
            arguments.putBoolean(MainActivity.IS_TWO_PANE_KEY, getIntent().getBooleanExtra(MainActivity.IS_TWO_PANE_KEY, false));

            Top10TracksFragment fragment = new Top10TracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top10tracks_container, fragment)
                    .commit();
        }
    }

    /*
     * Register for media player service broadcasts
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
     * Stop listening for broadcasts
     */
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top10_tracks, menu);
        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing);
        mShareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_now_playing:
                Intent intent = new Intent(this, MediaPlayerService.class).setAction(MediaPlayerService.MEDIA_REQUEST_CURRENT_TRACK_STATUS);
                startService(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackSelected(String artistName, TrackInfo trackInfo, int pos) {
        Intent playTrack = new Intent(this, PlayerActivity.class)
                .putExtra(PlayerFragment.ARTIST_NAME_KEY, artistName)
                .putExtra(PlayerFragment.TRACK_INFO_KEY, trackInfo)
                .putExtra(PlayerFragment.TRACK_INDEX_KEY, pos);
        playTrack.setAction(PlayerFragment.ACTION_LAUNCH_AND_PLAY);
        startActivity(playTrack);
    }
}
