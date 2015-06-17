package com.example.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * Displays an artist's top 10 tracks
 */
public class Top10TracksActivityFragment extends Fragment {

    private SpotifyTracksArrayAdapter mSpotifyTracksArrayAdapter;
    private String mId;
    private Toast toast;

    public Top10TracksActivityFragment() {
    }

    /**
     * Creates the list to display tracks
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top10_tracks, container, false);

        mId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
        String subtitle = getActivity().getIntent().getStringExtra(Intent.EXTRA_TITLE);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }

        TrackInfo trackInfo = new TrackInfo();

        mSpotifyTracksArrayAdapter = new SpotifyTracksArrayAdapter(getActivity(), trackInfo, R.layout.list_item_tracks, R.id.list_item_track_textview, R.id.list_item_album_textview, R.id.list_item_album_imageview);

        final ListView listView = (ListView) rootView.findViewById(R.id.list_view_tracks);
        listView.setAdapter(mSpotifyTracksArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Spotify Streamer stage 2 music preview
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        searchTop10Albums();

    }

    private void searchTop10Albums() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            new FetchTop10Albums().execute(mId);
        } else {
            displayToast(getString(R.string.toast_no_network_connectivity));
        }
    }

    private void displayToast(String message) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Searches Spotify for an artist's top 10 tracks and displays them in a list
     */
    public class FetchTop10Albums extends AsyncTask<String, Void, List<Track>> {
        private final String LOG_TAG = FetchTop10Albums.class.getSimpleName();

        @Override
        protected List<Track> doInBackground(String... ids) {
            Tracks results;

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                Map<String, Object> map = new HashMap<>();
                map.put("country", "PH");

                results = spotify.getArtistTopTrack(ids[0], map);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                displayToast(spotifyError.getMessage());
                return null;
            }

            return results.tracks;
        }


        @Override
        protected void onPostExecute(List<Track> tracks) {
            super.onPostExecute(tracks);
            if (tracks != null) {
                if (tracks.isEmpty()) {
                    displayToast(getString(R.string.toast_no_tracks_found));
                    return;
                }

                for (Track track : tracks) {

                    String largeThumbnail = "";
                    String mediumThumbnail = "";

                    if (track.album.images.get(0).url != null)
                        largeThumbnail = track.album.images.get(0).url;
                    if (track.album.images.get(1).url != null)
                        mediumThumbnail = track.album.images.get(1).url;

                    mSpotifyTracksArrayAdapter.add(track.name, track.album.name, mediumThumbnail, largeThumbnail, track.preview_url);
                }
            }
        }
    }
}
