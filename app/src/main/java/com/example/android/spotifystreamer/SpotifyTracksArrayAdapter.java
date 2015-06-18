package com.example.android.spotifystreamer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Displays an album image with track name and album name
 */
public class SpotifyTracksArrayAdapter extends ArrayAdapter<String> {
    private final TrackInfo mTrackInfo;
    private final int mResource;
    private final int mTextViewTrackId;
    private final int mTextViewAlbumId;
    private final int mImageViewThumbnailId;

    public SpotifyTracksArrayAdapter(Activity activity, TrackInfo trackInfo, int resource, int textViewTrackId, int textViewAlbumId, int imageViewThumbnailId) {
        super(activity, resource, trackInfo.getTrackNames());
        this.mTrackInfo = trackInfo;
        this.mResource = resource;
        this.mTextViewTrackId = textViewTrackId;
        this.mTextViewAlbumId = textViewAlbumId;
        this.mImageViewThumbnailId = imageViewThumbnailId;
    }

    /**
     * Creates a list item containing a thumbnail image, track name, and album name
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = inflater.inflate(mResource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.trackText = (TextView) convertView.findViewById(mTextViewTrackId);
            viewHolder.albumText = (TextView) convertView.findViewById(mTextViewAlbumId);
            viewHolder.albumImage = (ImageView) convertView.findViewById(mImageViewThumbnailId);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.trackText.setText(mTrackInfo.getTrackNames().get(position));
        viewHolder.albumText.setText(mTrackInfo.getAlbumNames().get(position));

        if (mTrackInfo.getMediumThumbnails().get(position).equals("")) {
            //Icon made by http://www.freepik.com from http://www.flaticon.com is licensed under Creative Commons BY 3.0
            viewHolder.albumImage.setImageResource(R.mipmap.album_placeholder);
        } else {
            Picasso.with(getContext()).load(mTrackInfo.getMediumThumbnails().get(position)).into(viewHolder.albumImage);
        }

        return convertView;
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

    private static class ViewHolder {
        TextView trackText;
        TextView albumText;
        ImageView albumImage;
    }
}
