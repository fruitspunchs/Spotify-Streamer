package com.example.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * Searches Spotify for a list of artists and display them.
 */
public class MainFragment extends Fragment {

    private static String ARTIST_INFO_KEY = "artistInfo";
    private static String SELECTED_POS_KEY = "selectedPos";
    private SpotifyArtistArrayAdapter mArtistAdapter;
    private ProgressBar mProgressBar;
    private ArtistInfo mArtistInfo;
    private int mSelectedPos = ListView.INVALID_POSITION;
    private ListView mListView;

    public MainFragment() {
    }

    /**
     * Creates the list to display artists and search box to find artists.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String LOG_TAG = this.getClass().getSimpleName();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (null == savedInstanceState) {
            mArtistInfo = new ArtistInfo();
        } else {
            mArtistInfo = savedInstanceState.getParcelable(ARTIST_INFO_KEY);
            mSelectedPos = savedInstanceState.getInt(SELECTED_POS_KEY);
        }

        mArtistAdapter = new SpotifyArtistArrayAdapter(getActivity(), mArtistInfo, R.layout.list_item_artist, R.id.list_item_artist_textview, R.id.list_item_artist_imageview);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar_artist_search);
        mProgressBar.setVisibility(View.GONE);

        mListView = (ListView) rootView.findViewById(R.id.list_view_artist);
        mListView.setAdapter(mArtistAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String artistId = mArtistAdapter.getId(position);
                String artistName = mArtistAdapter.getItem(position);
                mSelectedPos = position;

                ((Callback) getActivity()).onArtistSelected(artistId, artistName);

            }
        });

        final SearchView searchView = (SearchView) rootView.findViewById(R.id.artist_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ((Callback) getActivity()).onArtistSearch();
                mArtistAdapter.clear();
                searchArtists(query);
                mListView.clearChoices();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARTIST_INFO_KEY, mArtistInfo);
        outState.putInt(SELECTED_POS_KEY, mSelectedPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mArtistAdapter.notifyDataSetChanged();

        //Scroll to previously selected position
        if (mSelectedPos != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mSelectedPos);
        }
    }

    private void searchArtists(String query) {
        if (Utility.isNetworkConnected(getActivity())) {
            mProgressBar.setVisibility(View.VISIBLE);
            new FetchArtistTask().execute(query);
        }
    }

    /*
     * Used to communicate with activities containing this fragment
     */
    public interface Callback {
        void onArtistSelected(String id, String artistName);

        void onArtistSearch();
    }

    /**
     * Searches Spotify for artists and displays them in a list.
     */
    public class FetchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();

        boolean hasCaughtError = false;
        String errorString;

        @Override
        protected List<Artist> doInBackground(String... artist) {
            ArtistsPager results;

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                results = spotify.searchArtists(artist[0]);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                hasCaughtError = true;
                errorString = spotifyError.getMessage();
                return null;
            }

            return results.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);
            mArtistAdapter.clear();
            mProgressBar.setVisibility(View.GONE);

            if (hasCaughtError) {
                Utility.displayToast(getActivity(), errorString);
            }

            if (artists != null) {
                if (artists.isEmpty()) {
                    Utility.displayToast(getActivity(), getString(R.string.toast_no_artist_found));
                    return;
                }

                for (Artist artistItem : artists) {
                    if (artistItem.images.isEmpty()) {
                        mArtistAdapter.add(artistItem.name, "", artistItem.id);
                    } else {
                        int lastItem = artistItem.images.size() - 1;
                        mArtistAdapter.add(artistItem.name, artistItem.images.get(lastItem).url, artistItem.id);
                    }
                }
            }
        }
    }
}
