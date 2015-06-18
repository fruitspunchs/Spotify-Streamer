package com.example.android.spotifystreamer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Random;

/**
 * Displays an album image with track name and album name
 */
public class SpotifyTracksArrayAdapter extends ArrayAdapter<String> {
    private final Activity mActivity;
    private final TrackInfo mTrackInfo;
    private final int mResource;
    private final int textViewTrackId;
    private final int textViewAlbumId;
    private final int imageViewThumbnailId;
    /**
     * Assign a random color to album with no images
     */
    private final Random mRandomNumber = new Random();

    public SpotifyTracksArrayAdapter(Activity activity, TrackInfo trackInfo, int resource, int textViewTrackId, int textViewAlbumId, int imageViewThumbnailId) {
        super(activity, resource, trackInfo.getTrackNames());
        this.mActivity = activity;
        this.mTrackInfo = trackInfo;
        this.mResource = resource;
        this.textViewTrackId = textViewTrackId;
        this.textViewAlbumId = textViewAlbumId;
        this.imageViewThumbnailId = imageViewThumbnailId;
    }

    /**
     * Creates a list item containing a thumbnail image, track name, and album name
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mActivity.getLayoutInflater();

        //Recycling view causes item imageView to show pictures from other artists if you scroll fast repeatedly
        View rootView = inflater.inflate(mResource, parent, false);

        TextView txtTrack = (TextView) rootView.findViewById(textViewTrackId);
        txtTrack.setText(mTrackInfo.getTrackNames().get(position));

        TextView txtAlbum = (TextView) rootView.findViewById(textViewAlbumId);
        txtAlbum.setText(mTrackInfo.getAlbumNames().get(position));

        ImageView imageView = (ImageView) rootView.findViewById(imageViewThumbnailId);

        if (mTrackInfo.getMediumThumbnails().get(position).equals("")) {
        } else {
            Picasso.with(mActivity).load(mTrackInfo.getMediumThumbnails().get(position)).into(imageView);
        }

        return rootView;
    }

    /**
     * Clears all stored track values
     */
    @Override
    public void clear() {
        super.clear();
        mTrackInfo.clear();
    }

    public void add(String track, String album, String mediumThumbnail, String largeThumbnail, String trackUrl) {
        super.add(track);
        mTrackInfo.getAlbumNames().add(album);
        mTrackInfo.getMediumThumbnails().add(mediumThumbnail);
        mTrackInfo.getLargeThumbnails().add(largeThumbnail);
        mTrackInfo.getTrackPreviewUrls().add(trackUrl);
    }
}
