package com.example.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

/**
 * Service that streams music given a url
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "com.example.android.spotifystreamer.PLAY";
    public static final String ACTION_PAUSE = "com.example.android.spotifystreamer.PAUSE";
    public static final String ACTION_NEXT = "com.example.android.spotifystreamer.NEXT";
    public static final String ACTION_PREVIOUS = "com.example.android.spotifystreamer.PREVIOUS";
    private static String LOG_TAG;

    private MediaPlayer mMediaPlayer;
    private boolean misRecoveringFromError = false;
    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private String mNowPlayingUrl = "";
    private boolean mIsPrepared = false;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Override
    public void onCreate() {
        LOG_TAG = getClass().getSimpleName();

        HandlerThread thread = new HandlerThread("MediaPlayerThread");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setScreenOnWhilePlaying(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "Service started");

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    //TODO: extend Service instead of IntentService
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "Service destroyed");
        mMediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playTrack(String urlString) {
        if (Utility.isNetworkConnected(this)) {
            try {
                mMediaPlayer.setDataSource(urlString);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                Utility.displayToast(this, getString(R.string.error_unable_to_play_track));
            }
        }
    }

    private void playMedia() {
        mMediaPlayer.start();
        //mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void pauseMedia() {
        //mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPrepared = true;

        if (!misRecoveringFromError) {
            playMedia();
        } else {
            misRecoveringFromError = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        //TODO: temp code to dispose itself
        stopSelf();
    }

    private void resetMediaOnError() {
        misRecoveringFromError = true;
        mIsPrepared = false;
        mMediaPlayer.reset();
        playTrack(mTrackInfo.getTrackPreviewUrls().get(mTrackPosition));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Utility.displayToast(this, getString(R.string.error_playback));
        resetMediaOnError();
        return false;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                Intent intent = (Intent) msg.obj;

                if (intent.getAction().equals(ACTION_PLAY)) {
                    mTrackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
                    mTrackPosition = intent.getIntExtra(PlayerFragment.TRACK_POSITION_KEY, -1);

                    String intentTrackUrl = mTrackInfo.getTrackPreviewUrls().get(mTrackPosition);

                    if (mNowPlayingUrl.equals(intentTrackUrl) && mIsPrepared) {
                        playMedia();
                    } else {
                        mNowPlayingUrl = intentTrackUrl;
                        playTrack(mNowPlayingUrl);
                    }
                }
            }
        }
    }
}
