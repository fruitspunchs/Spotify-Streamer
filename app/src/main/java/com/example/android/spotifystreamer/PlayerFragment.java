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

    public static String DIALOG_FIRST_OPEN_KEY = "dialogFirstOpen";

    public static String ACTION_LAUNCH_FROM_NOTIFICATION = "com.example.android.spotifystreamer.ACTION_LAUNCH_FROM_NOTIFICATION";
    public static String ACTION_FIRST_LAUNCH_FROM_ACTIVITY = "com.example.android.spotifystreamer.ACTION_FIRST_LAUNCH_FROM_ACTIVITY";
    public static String ACTION_NON_FIRST_LAUNCH_FROM_ACTIVITY = "com.example.android.spotifystreamer.ACTION_NON_FIRST_LAUNCH_FROM_ACTIVITY";
    boolean mIsFirstDialogOpen = false;
    private String mArtistName;
    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private ImageButton mPlayPauseButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        if (null == savedInstanceState) {
            mArtistName = getArguments().getString(ARTIST_NAME_KEY);
            mTrackInfo = getArguments().getParcelable(TRACK_INFO_KEY);
            mTrackPosition = getArguments().getInt(TRACK_POSITION_KEY);
        } else {
            mArtistName = savedInstanceState.getString(ARTIST_NAME_KEY);
            mTrackInfo = savedInstanceState.getParcelable(TRACK_INFO_KEY);
            mTrackPosition = savedInstanceState.getInt(TRACK_POSITION_KEY);
        }

        TextView artistText = (TextView) rootView.findViewById(R.id.artist_name_textview);
        artistText.setText(mArtistName);

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


        if (mIsFirstDialogOpen) {
            Intent intent = new Intent(MediaPlayerService.ACTION_PLAY, null, getActivity(), MediaPlayerService.class).putExtra(TRACK_INFO_KEY, mTrackInfo).putExtra(TRACK_POSITION_KEY, mTrackPosition).putExtra(ARTIST_NAME_KEY, mArtistName);
            getActivity().startService(intent);
            getArguments().putBoolean(DIALOG_FIRST_OPEN_KEY, false);
        } else if (getActivity().getIntent() != null) {
            if (getActivity().getIntent().getAction().equals(ACTION_FIRST_LAUNCH_FROM_ACTIVITY)) {
                Intent intent = new Intent(MediaPlayerService.ACTION_PLAY, null, getActivity(), MediaPlayerService.class).putExtra(TRACK_INFO_KEY, mTrackInfo).putExtra(TRACK_POSITION_KEY, mTrackPosition).putExtra(ARTIST_NAME_KEY, mArtistName);
                getActivity().startService(intent);
                getActivity().getIntent().setAction(ACTION_NON_FIRST_LAUNCH_FROM_ACTIVITY);
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARTIST_NAME_KEY, mArtistName);
        outState.putParcelable(TRACK_INFO_KEY, mTrackInfo);
        outState.putInt(TRACK_POSITION_KEY, mTrackPosition);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mIsFirstDialogOpen = getArguments().getBoolean(DIALOG_FIRST_OPEN_KEY);

        return dialog;
    }
}
