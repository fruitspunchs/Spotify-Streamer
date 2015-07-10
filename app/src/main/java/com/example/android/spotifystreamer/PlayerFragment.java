package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by User on 7/10/2015.
 */
public class PlayerFragment extends DialogFragment {
    public static String ARTIST_NAME_KEY = "artistName";
    public static String TRACK_INFO_KEY = "trackInfo";
    public static String TRACK_POSITION_KEY = "trackPosition";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        String artistName = getArguments().getString(ARTIST_NAME_KEY);
        TrackInfo trackInfo = getArguments().getParcelable(TRACK_INFO_KEY);
        int trackPosition = getArguments().getInt(TRACK_POSITION_KEY);

        TextView artistText = (TextView) rootView.findViewById(R.id.artist_name_textview);
        artistText.setText(artistName);

        TextView trackText = (TextView) rootView.findViewById(R.id.track_name_textview);
        trackText.setText(trackInfo.getTrackNames().get(trackPosition));

        ImageView albumImage = (ImageView) rootView.findViewById(R.id.album_image);
        String trackUrl = trackInfo.getMediumThumbnails().get(trackPosition);
        if (trackUrl != "") {
            Picasso.with(getActivity()).load(trackUrl).into(albumImage);
        } else {
            albumImage.setImageResource(R.mipmap.album_placeholder);
        }

        TextView albumText = (TextView) rootView.findViewById(R.id.album_name_textview);
        albumText.setText(trackInfo.getAlbumNames().get(trackPosition));

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
