package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class PlayerActivity extends AppCompatActivity {
    private static String PLAYER_FRAGMENT_TAG = "playerFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
