package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlayerFragment extends DialogFragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static String ARTIST_NAME_KEY = "artistName";
    public static String TRACK_INFO_KEY = "trackInfo";
    public static String TRACK_POSITION_KEY = "trackPosition";

    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private MediaPlayer mMediaPlayer;
    private Toast mToast;
    private ImageButton mPlayPauseButton;
    private boolean isResetFromError = false;

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

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        playTrack(mTrackInfo.getTrackPreviewUrls().get(mTrackPosition));
    }

    private void playTrack(String urlString) {
        try {
            mMediaPlayer.setDataSource(urlString);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Utility.displayToast(getActivity(), mToast, getString(R.string.error_unable_to_play_track));
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.release();
        super.onDestroy();
    }

    private void playMedia() {
        mMediaPlayer.start();
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void pauseMedia() {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!isResetFromError) {
            playMedia();
        } else {
            isResetFromError = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    private void resetMedia() {
        isResetFromError = true;
        mMediaPlayer.reset();
        playTrack(mTrackInfo.getTrackPreviewUrls().get(mTrackPosition));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Utility.displayToast(getActivity(), mToast, getString(R.string.error_playback));
        return false;
    }
}
