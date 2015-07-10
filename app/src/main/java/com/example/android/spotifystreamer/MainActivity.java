package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements MainFragment.ItemSelectedCallback, Top10TracksFragment.ItemSelectedCallback {

    public static String ARTIST_ID_KEY = "artistId";
    public static String ARTIST_NAME_KEY = "artistName";
    public static String IS_TWO_PANE_KEY = "isTwoPane";
    private static String PLAYER_FRAGMENT_TAG = "playerFragment";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.top10tracks_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.top10tracks_container, new Top10TracksFragment()).commit();
            }
        } else {
            mTwoPane = false;
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
    public void onArtistSelected(String id, String artistName) {
        if (mTwoPane) {
            Top10TracksFragment fragment = new Top10TracksFragment();
            Bundle args = new Bundle();
            args.putString(ARTIST_ID_KEY, id);
            args.putString(ARTIST_NAME_KEY, artistName);
            args.putBoolean(IS_TWO_PANE_KEY, mTwoPane);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.top10tracks_container, fragment).commit();
        } else {
            Intent seeTop10Tracks = new Intent(this, Top10TracksActivity.class).putExtra(ARTIST_ID_KEY, id).putExtra(ARTIST_NAME_KEY, artistName).putExtra(IS_TWO_PANE_KEY, mTwoPane);
            startActivity(seeTop10Tracks);
        }
    }

    @Override
    public void onTrackSelected(String artistName, TrackInfo trackInfo, int pos) {
        PlayerFragment fragment = new PlayerFragment();
        fragment.show(getSupportFragmentManager(), PLAYER_FRAGMENT_TAG);
    }
}
