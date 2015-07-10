package com.example.android.spotifystreamer;

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

        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(PlayerFragment.ARTIST_NAME_KEY, getIntent().getStringExtra(PlayerFragment.ARTIST_NAME_KEY));
        args.putParcelable(PlayerFragment.TRACK_INFO_KEY, getIntent().getParcelableExtra(PlayerFragment.TRACK_INFO_KEY));
        args.putInt(PlayerFragment.TRACK_POSITION_KEY, getIntent().getIntExtra(PlayerFragment.TRACK_POSITION_KEY, 0));
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
