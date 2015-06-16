package com.example.android.spotifystreamer;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Random;

/**
 * Displays an artist image with name
 */
public class SpotifyArtistArrayAdapter extends ArrayAdapter<String> {
    private final Activity mActivity;
    private final int mResource;
    private final int mTextViewResourceId;
    private final int mImageViewResourceId;
    private final ArtistInfo mArtistInfo;

    public SpotifyArtistArrayAdapter(Activity activity,
                                     ArtistInfo artistInfo, int resource, int textViewResourceId, int imageViewResourceId) {
        super(activity, resource, artistInfo.getArtistNames());
        this.mActivity = activity;
        this.mArtistInfo = artistInfo;
        this.mResource = resource;
        this.mTextViewResourceId = textViewResourceId;
        this.mImageViewResourceId = imageViewResourceId;

    }

    /**
     * Creates a list item containing an artist image with name
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mActivity.getLayoutInflater();

        View rootView = null;

        if (view != null) {
            rootView = view;
        } else {
            rootView = inflater.inflate(mResource, parent, false);
        }

        TextView txtTitle = (TextView) rootView.findViewById(mTextViewResourceId);

        ImageView imageView = (ImageView) rootView.findViewById(mImageViewResourceId);

        txtTitle.setText(mArtistInfo.getArtistNames().get(position));

        if (mArtistInfo.getArtistImages().get(position).equals("")) {
            imageView.setBackgroundColor(mArtistInfo.getDefaultColors().get(position));
        } else {
            Picasso.with(mActivity).load(mArtistInfo.getArtistImages().get(position)).into(imageView);
        }

        return rootView;
    }

    /**
     * Clears all stored artist values
     */
    @Override
    public void clear() {
        super.clear();
        mArtistInfo.clear();
    }

    /**
     * Assign a random color to artist with no images
     */
    private final Random mRandomNumber = new Random();

    private int randomColor() {
        return Color.rgb(mRandomNumber.nextInt(256), mRandomNumber.nextInt(256), mRandomNumber.nextInt(256));
    }

    public void add(String object, String imageUrl, String id) {
        super.add(object);
        mArtistInfo.getArtistImages().add(imageUrl);
        mArtistInfo.getDefaultColors().add(randomColor());
        mArtistInfo.getIds().add(id);
    }

    public String getId(int index) {
        return mArtistInfo.getIds().get(index);
    }


}
