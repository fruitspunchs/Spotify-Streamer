package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
    private static String LOG_TAG;
    private boolean mIsFirstDialogOpen = false;
    private boolean mIsDialogMode = false;
    private String mArtistName;
    private TrackInfo mTrackInfo;
    private int mTrackPosition;

    private TextView mArtistText;
    private TextView mTrackText;
    private TextView mAlbumText;
    private ImageView mAlbumImage;
    private ImageButton mPlayPauseButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private SeekBar mTrackSeekBar;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MediaPlayerService.MEDIA_EVENT_KEY);
            if (!message.equals(MediaPlayerService.MEDIA_EVENT_TRACK_PROGRESS)) {
                Log.d(LOG_TAG, "Got message: " + message);
            }

            switch (message) {
                case MediaPlayerService.ACTION_PLAY:
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    mPlayPauseButton.setTag(MediaPlayerService.ACTION_PAUSE);
                    break;
                case MediaPlayerService.ACTION_PAUSE:
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    mPlayPauseButton.setTag(MediaPlayerService.ACTION_PLAY);
                    break;
                case MediaPlayerService.ACTION_NEXT:
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    mTrackPosition = intent.getIntExtra(TRACK_POSITION_KEY, 0);
                    populatePlayerView(mTrackPosition);
                    break;
                case MediaPlayerService.ACTION_PREVIOUS:
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    mTrackPosition = intent.getIntExtra(TRACK_POSITION_KEY, 0);
                    populatePlayerView(mTrackPosition);
                    break;
                case MediaPlayerService.ACTION_STOP:
                    if (mIsDialogMode) {
                        dismiss();
                    } else {
                        getActivity().finish();
                    }
                    break;
                case MediaPlayerService.MEDIA_EVENT_BUFFERING_PROGRESS:
                    int bufferPercent = intent.getIntExtra(MediaPlayerService.BUFFER_PROGRESS_KEY, 0);
                    mTrackSeekBar.setSecondaryProgress(bufferPercent);
                    break;
                case MediaPlayerService.MEDIA_EVENT_TRACK_PROGRESS:
                    int trackProgress = intent.getIntExtra(MediaPlayerService.TRACK_PROGRESS_KEY, 0);
                    mTrackSeekBar.setProgress(trackProgress);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = this.getClass().getSimpleName();

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        mArtistName = getArguments().getString(ARTIST_NAME_KEY);
        mTrackInfo = getArguments().getParcelable(TRACK_INFO_KEY);
        mTrackPosition = getArguments().getInt(TRACK_POSITION_KEY);

        mArtistText = (TextView) rootView.findViewById(R.id.artist_name_textview);
        mArtistText.setText(mArtistName);

        mTrackText = (TextView) rootView.findViewById(R.id.track_name_textview);
        mTrackText.setText(mTrackInfo.getTrackNames().get(mTrackPosition));

        mAlbumImage = (ImageView) rootView.findViewById(R.id.album_image);
        String trackUrl = mTrackInfo.getMediumThumbnails().get(mTrackPosition);
        if (!trackUrl.equals("")) {
            Picasso.with(getActivity()).load(trackUrl).into(mAlbumImage);
        } else {
            mAlbumImage.setImageDrawable(Utility.randomColorDrawable());
        }

        mAlbumText = (TextView) rootView.findViewById(R.id.album_name_textview);
        mAlbumText.setText(mTrackInfo.getAlbumNames().get(mTrackPosition));

        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);
        mPreviousButton = (ImageButton) rootView.findViewById(R.id.previous_button);
        mNextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        mTrackSeekBar = (SeekBar) rootView.findViewById(R.id.track_seekbar);
        mTrackSeekBar.setMax(100);

        setPlayerControlListeners();

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

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(MediaPlayerService.MEDIA_EVENT));

        return rootView;
    }

    private void setPlayerControlListeners() {
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPauseButton.getTag().equals(MediaPlayerService.ACTION_PLAY)) {
                    Intent intent = new Intent(getActivity(), MediaPlayerService.class).setAction(MediaPlayerService.ACTION_PLAY);
                    getActivity().startService(intent);
                } else if (mPlayPauseButton.getTag().equals(MediaPlayerService.ACTION_PAUSE)) {
                    Intent intent = new Intent(getActivity(), MediaPlayerService.class).setAction(MediaPlayerService.ACTION_PAUSE);
                    getActivity().startService(intent);
                } else {
                    Log.d(LOG_TAG, "No tag set to play pause button");
                }
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MediaPlayerService.class).setAction(MediaPlayerService.ACTION_PREVIOUS);
                getActivity().startService(intent);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MediaPlayerService.class).setAction(MediaPlayerService.ACTION_NEXT);
                getActivity().startService(intent);
            }
        });

        mTrackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.d(LOG_TAG, "User seek");
                    Intent intent = new Intent(getActivity(), MediaPlayerService.class).setAction(MediaPlayerService.ACTION_SEEK).putExtra(MediaPlayerService.TRACK_SEEK_POSITION_KEY, progress);
                    getActivity().startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void populatePlayerView(int trackPosition) {
        mTrackSeekBar.setProgress(0);
        mTrackSeekBar.setSecondaryProgress(0);
        mArtistText.setText(mArtistName);
        mTrackText.setText(mTrackInfo.getTrackNames().get(trackPosition));
        mAlbumText.setText(mTrackInfo.getAlbumNames().get(mTrackPosition));
        String trackUrl = mTrackInfo.getMediumThumbnails().get(trackPosition);
        if (!trackUrl.equals("")) {
            Picasso.with(getActivity()).load(trackUrl).into(mAlbumImage);
        } else {
            mAlbumImage.setImageDrawable(Utility.randomColorDrawable());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mIsFirstDialogOpen = getArguments().getBoolean(DIALOG_FIRST_OPEN_KEY);
        mIsDialogMode = true;

        return dialog;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
