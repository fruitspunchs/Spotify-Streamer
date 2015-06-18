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
    private final int mResource;
    private final int mTextViewResourceId;
    private final int mImageViewResourceId;
    private final ArtistInfo mArtistInfo;
    /**
     * Assign a random color to artist with no images
     */
    private final Random mRandomNumber = new Random();

    public SpotifyArtistArrayAdapter(Activity activity,
                                     ArtistInfo artistInfo, int resource, int textViewResourceId, int imageViewResourceId) {
        super(activity, resource, artistInfo.getArtistNames());
        this.mArtistInfo = artistInfo;
        this.mResource = resource;
        this.mTextViewResourceId = textViewResourceId;
        this.mImageViewResourceId = imageViewResourceId;

    }

    /**
     * Creates a list item containing an artist image with name
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = inflater.inflate(mResource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(mTextViewResourceId);
            viewHolder.image = (ImageView) convertView.findViewById(mImageViewResourceId);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.text.setText(mArtistInfo.getArtistNames().get(position));

        if (mArtistInfo.getArtistImages().get(position).equals("")) {
            //<div>Icon made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed under <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a></div>
            viewHolder.image.setImageResource(R.mipmap.artist_placeholder);
        } else {
            Picasso.with(getContext()).load(mArtistInfo.getArtistImages().get(position)).into(viewHolder.image);
        }

        return convertView;
    }

    /**
     * Clears all stored artist values
     */
    @Override
    public void clear() {
        super.clear();
        mArtistInfo.clear();
    }

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

    private static class ViewHolder {
        TextView text;
        ImageView image;
    }


}
