package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class Top10TracksActivity extends AppCompatActivity implements Top10TracksFragment.ItemSelectedCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_tracks);

        if (null == savedInstanceState) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackSelected(String artistName, TrackInfo trackInfo, int pos) {
        Intent playTrack = new Intent(this, PlayerActivity.class)
                .putExtra(PlayerFragment.ARTIST_NAME_KEY, artistName)
                .putExtra(PlayerFragment.TRACK_INFO_KEY, trackInfo)
                .putExtra(PlayerFragment.TRACK_POSITION_KEY, pos);
        playTrack.setAction(PlayerFragment.ACTION_FIRST_LAUNCH_FROM_ACTIVITY);
        startActivity(playTrack);
    }
}
