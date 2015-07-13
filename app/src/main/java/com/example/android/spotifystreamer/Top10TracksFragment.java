package com.example.android.spotifystreamer;

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
import android.widget.ProgressBar;

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
public class Top10TracksFragment extends Fragment {

    private final static String TRACK_INFO_KEY = "trackInfo";
    private static String SELECTED_POS_KEY = "selectedPos";
    private SpotifyTracksArrayAdapter mSpotifyTracksArrayAdapter;
    private String mId;
    private ProgressBar mProgressBar;
    private String mArtistName;
    private TrackInfo mTrackInfo;
    private int mSelectedPos = ListView.INVALID_POSITION;
    private ListView mListView;

    public Top10TracksFragment() {
    }

    /**
     * Creates the list to display tracks
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top10_tracks, container, false);

        String subtitle = "";
        Boolean isTwoPane = false;

        if (getArguments() != null) {
            mId = getArguments().getString(MainActivity.ARTIST_ID_KEY);
            subtitle = getArguments().getString(MainActivity.ARTIST_NAME_KEY);
            mArtistName = subtitle;
            isTwoPane = getArguments().getBoolean(MainActivity.IS_TWO_PANE_KEY);
        }

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar_top_10_tracks);
        mProgressBar.setVisibility(View.GONE);

        if (!isTwoPane) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(subtitle);
            }
        }

        if (null == savedInstanceState) {
            mTrackInfo = new TrackInfo();
        } else {
            mTrackInfo = savedInstanceState.getParcelable(TRACK_INFO_KEY);
            mSelectedPos = savedInstanceState.getInt(SELECTED_POS_KEY);
        }

        mSpotifyTracksArrayAdapter = new SpotifyTracksArrayAdapter(getActivity(), mTrackInfo, R.layout.list_item_tracks, R.id.list_item_track_textview, R.id.list_item_album_textview, R.id.list_item_album_imageview);


        mListView = (ListView) rootView.findViewById(R.id.list_view_tracks);
        mListView.setAdapter(mSpotifyTracksArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((ItemSelectedCallback) getActivity()).onTrackSelected(mArtistName, mTrackInfo, position);
                mSelectedPos = position;
            }
        });

        if (mTrackInfo.isEmpty()) {
            searchTop10Albums();
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(TRACK_INFO_KEY, mTrackInfo);
        outState.putInt(SELECTED_POS_KEY, mSelectedPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSelectedPos != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mSelectedPos);
        }
    }

    private void searchTop10Albums() {
        if (null == mId) {
            return;
        }

        if (Utility.isNetworkConnected(getActivity())) {
            mProgressBar.setVisibility(View.VISIBLE);
            new FetchTop10Albums().execute(mId);
        }
    }

    public interface ItemSelectedCallback {
        void onTrackSelected(String artistName, TrackInfo trackInfo, int pos);
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
                Utility.displayToast(getActivity(), spotifyError.getMessage());
                return null;
            }

            return results.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            super.onPostExecute(tracks);
            mProgressBar.setVisibility(View.GONE);
            mSpotifyTracksArrayAdapter.clear();
            if (tracks != null) {
                if (tracks.isEmpty()) {
                    Utility.displayToast(getActivity(), getString(R.string.toast_no_tracks_found));
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
