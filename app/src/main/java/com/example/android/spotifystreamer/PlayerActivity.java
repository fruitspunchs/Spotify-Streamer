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

public class PlayerActivity extends AppCompatActivity {
    private static String LOG_TAG;
    private static String PLAYER_FRAGMENT_TAG = "playerFragment";
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
                    mShareActionProvider.setShareIntent(Utility.createShareTrackIntent(intent.getStringExtra(MediaPlayerService.TRACK_URL_KEY)));
                    mShareItem.setVisible(true);
                    break;
                case MediaPlayerService.MEDIA_EVENT_NOT_PLAYING:
                    mShareItem.setVisible(false);
                    break;
            }
        }
    };

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

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(MediaPlayerService.MEDIA_EVENT));
        Intent requestServiceIsTrackLoaded = new Intent(this, MediaPlayerService.class).setAction(MediaPlayerService.MEDIA_EVENT_IS_TRACK_LOADED);
        startService(requestServiceIsTrackLoaded);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mShareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
        return super.onPrepareOptionsMenu(menu);
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
