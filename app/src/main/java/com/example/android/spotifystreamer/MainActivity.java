package com.example.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements MainFragment.Callback, Top10TracksFragment.ItemSelectedCallback {

    public static String ARTIST_ID_KEY = "artistId";
    public static String ARTIST_NAME_KEY = "artistName";
    public static String IS_TWO_PANE_KEY = "isTwoPane";
    private static String PLAYER_FRAGMENT_TAG = "playerFragment";
    private static String TOP_10_TRACKS_FRAGMENT_TAG = "top10TracksFragment";
    private String LOG_TAG;
    private boolean mTwoPane;
    private MenuItem mNowPlayingMenuItem;

    private MenuItem mShareItem;
    private ShareActionProvider mShareActionProvider;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MediaPlayerService.MEDIA_EVENT_KEY);
            if (!message.equals(MediaPlayerService.MEDIA_EVENT_TRACK_PROGRESS)) {
                Log.d(LOG_TAG, "Got message: " + message);
            }

            switch (message) {
                case MediaPlayerService.MEDIA_EVENT_PLAYING:
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
                case MediaPlayerService.MEDIA_EVENT_NOT_PLAYING:
                    if (mNowPlayingMenuItem != null) {
                        mNowPlayingMenuItem.setVisible(false);
                    }
                    if (mShareItem != null) {
                        mShareItem.setVisible(false);
                    }
                    break;
                case MediaPlayerService.MEDIA_EVENT_REPLY_NOW_PLAYING:
                    Intent showPlayerIntent;
                    String artistName = intent.getStringExtra(PlayerFragment.ARTIST_NAME_KEY);
                    TrackInfo trackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
                    int trackPosition = intent.getIntExtra(PlayerFragment.TRACK_INDEX_KEY, 0);
                    int bufferProgress = intent.getIntExtra(MediaPlayerService.BUFFER_PROGRESS_KEY, 0);
                    boolean isPlaying = intent.getBooleanExtra(MediaPlayerService.IS_PLAYING_KEY, false);
                    int seekBarPosition = intent.getIntExtra(MediaPlayerService.TRACK_PROGRESS_KEY, 0);

                    if (mTwoPane) {
                        PlayerFragment fragment = new PlayerFragment();
                        Bundle args = new Bundle();
                        args.putString(PlayerFragment.ARTIST_NAME_KEY, artistName);
                        args.putParcelable(PlayerFragment.TRACK_INFO_KEY, trackInfo);
                        args.putInt(PlayerFragment.TRACK_INDEX_KEY, trackPosition);
                        args.putInt(MediaPlayerService.BUFFER_PROGRESS_KEY, bufferProgress);
                        args.putBoolean(MediaPlayerService.IS_PLAYING_KEY, isPlaying);
                        args.putInt(MediaPlayerService.TRACK_PROGRESS_KEY, seekBarPosition);
                        args.putBoolean(PlayerFragment.DIALOG_FIRST_OPEN_KEY, false);
                        fragment.setArguments(args);
                        fragment.show(getSupportFragmentManager(), PLAYER_FRAGMENT_TAG);
                    } else {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_TAG = getClass().getSimpleName();
        setContentView(R.layout.activity_main);

        mTwoPane = findViewById(R.id.top10tracks_container) != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(MediaPlayerService.MEDIA_EVENT));

        if (mNowPlayingMenuItem != null) {
            Intent requestServiceIsTrackLoaded = new Intent(this, MediaPlayerService.class).setAction(MediaPlayerService.MEDIA_EVENT_IS_TRACK_LOADED);
            startService(requestServiceIsTrackLoaded);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing);
        mShareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_now_playing:
                Intent intent = new Intent(this, MediaPlayerService.class).setAction(MediaPlayerService.MEDIA_EVENT_REQUEST_NOW_PLAYING);
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
    public void onArtistSelected(String id, String artistName) {
        if (mTwoPane) {
            Top10TracksFragment fragment = new Top10TracksFragment();
            Bundle args = new Bundle();
            args.putString(ARTIST_ID_KEY, id);
            args.putString(ARTIST_NAME_KEY, artistName);
            args.putBoolean(IS_TWO_PANE_KEY, mTwoPane);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.top10tracks_container, fragment, TOP_10_TRACKS_FRAGMENT_TAG).commit();
        } else {
            Intent seeTop10Tracks = new Intent(this, Top10TracksActivity.class).putExtra(ARTIST_ID_KEY, id).putExtra(ARTIST_NAME_KEY, artistName).putExtra(IS_TWO_PANE_KEY, mTwoPane);
            startActivity(seeTop10Tracks);
        }
    }

    @Override
    public void onArtistSearch() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TOP_10_TRACKS_FRAGMENT_TAG);
        if (null != fragment) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onTrackSelected(String artistName, TrackInfo trackInfo, int pos) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(PlayerFragment.ARTIST_NAME_KEY, artistName);
        args.putParcelable(PlayerFragment.TRACK_INFO_KEY, trackInfo);
        args.putInt(PlayerFragment.TRACK_INDEX_KEY, pos);
        args.putInt(MediaPlayerService.BUFFER_PROGRESS_KEY, 0);
        args.putBoolean(PlayerFragment.DIALOG_FIRST_OPEN_KEY, true);
        fragment.setArguments(args);

        fragment.show(getSupportFragmentManager(), PLAYER_FRAGMENT_TAG);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MediaPlayerService.class);
        stopService(intent);
    }
}
