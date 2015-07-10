package com.example.android.spotifystreamer;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Displays an artist image with name
 */
public class SpotifyArtistArrayAdapter extends ArrayAdapter<String> {
    private final int mResource;
    private final int mTextViewResourceId;
    private final int mImageViewResourceId;
    private final ArtistInfo mArtistInfo;

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
            viewHolder.randomColorDrawable = Utility.randomColorDrawable();

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.text.setText(mArtistInfo.getArtistNames().get(position));

        if (mArtistInfo.getArtistImages().get(position).equals("")) {
            viewHolder.image.setImageDrawable(viewHolder.randomColorDrawable);
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

    public void add(String object, String imageUrl, String id) {
        super.add(object);
        mArtistInfo.getArtistImages().add(imageUrl);
        mArtistInfo.getIds().add(id);
    }

    public String getId(int index) {
        return mArtistInfo.getIds().get(index);
    }

    private static class ViewHolder {
        TextView text;
        ImageView image;
        ColorDrawable randomColorDrawable;
    }


}
