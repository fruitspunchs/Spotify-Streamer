package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * Search Spotify for a list of artists and display them
 */
public class MainActivityFragment extends Fragment {

    private SpotifyArtistArrayAdapter mArtistAdapter;

    public MainActivityFragment() {
    }

    /**
     * Creates the list to display artists and search box to find artists
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String LOG_TAG = this.getClass().getSimpleName();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArtistInfo mArtistInfo = new ArtistInfo();
        mArtistAdapter = new SpotifyArtistArrayAdapter(getActivity(), mArtistInfo, R.layout.list_item_artist, R.id.list_item_artist_textview, R.id.list_item_artist_imageview);

        final ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist);
        listView.setAdapter(mArtistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String artistId = mArtistAdapter.getId(position);
                String artistName = mArtistAdapter.getItem(position);
                Intent seeTop10Tracks = new Intent(getActivity(), Top10TracksActivity.class).putExtra(Intent.EXTRA_TEXT, artistId).putExtra(Intent.EXTRA_TITLE, artistName);

                startActivity(seeTop10Tracks);

            }
        });

        final SearchView searchView = (SearchView) rootView.findViewById(R.id.search_view_artist);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                new FetchArtistTask().execute(query);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Searches Spotify for artists and displays them in a list
     */
    public class FetchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();
        private Toast toast;

        @Override
        protected List<Artist> doInBackground(String... artist) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(artist[0]);

            return results.artists.items;
        }

        private void displayToast(String message) {
            if (toast != null) {
                toast.cancel();
            }

            toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);
            mArtistAdapter.clear();

            if (artists != null) {
                if (artists.isEmpty()) {
                    displayToast(getString(R.string.toast_no_artist_found));
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
            } else {
                displayToast(getString(R.string.toast_no_artist_found));
            }
        }
    }
}
