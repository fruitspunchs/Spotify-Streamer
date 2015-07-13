package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PlayerFragment extends DialogFragment {
    public static String ARTIST_NAME_KEY = "artistName";
    public static String TRACK_INFO_KEY = "trackInfo";
    public static String TRACK_POSITION_KEY = "trackPosition";

    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private ImageButton mPlayPauseButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        String artistName = getArguments().getString(ARTIST_NAME_KEY);
        mTrackInfo = getArguments().getParcelable(TRACK_INFO_KEY);
        mTrackPosition = getArguments().getInt(TRACK_POSITION_KEY);

        TextView artistText = (TextView) rootView.findViewById(R.id.artist_name_textview);
        artistText.setText(artistName);

        TextView trackText = (TextView) rootView.findViewById(R.id.track_name_textview);
        trackText.setText(mTrackInfo.getTrackNames().get(mTrackPosition));

        ImageView albumImage = (ImageView) rootView.findViewById(R.id.album_image);
        String trackUrl = mTrackInfo.getMediumThumbnails().get(mTrackPosition);
        if (!trackUrl.equals("")) {
            Picasso.with(getActivity()).load(trackUrl).into(albumImage);
        } else {
            albumImage.setImageDrawable(Utility.randomColorDrawable());
        }

        TextView albumText = (TextView) rootView.findViewById(R.id.album_name_textview);
        albumText.setText(mTrackInfo.getAlbumNames().get(mTrackPosition));

        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);

        Intent intent = new Intent(MediaPlayerService.ACTION_PLAY, null, getActivity(), MediaPlayerService.class).putExtra(TRACK_INFO_KEY, mTrackInfo).putExtra(TRACK_POSITION_KEY, mTrackPosition);
        getActivity().startService(intent);

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
