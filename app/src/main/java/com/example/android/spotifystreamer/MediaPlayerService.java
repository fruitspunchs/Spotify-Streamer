package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;


/**
 * Service that streams music given a url
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "com.example.android.spotifystreamer.PLAY";
    public static final String ACTION_PAUSE = "com.example.android.spotifystreamer.PAUSE";
    public static final String ACTION_NEXT = "com.example.android.spotifystreamer.NEXT";
    public static final String ACTION_PREVIOUS = "com.example.android.spotifystreamer.PREVIOUS";
    private static final int MUSIC_PLAYER_NOTIFICATION_ID = 100;
    private static String LOG_TAG;
    private MediaPlayer mMediaPlayer;
    private boolean mIsRecoveringFromError = false;
    private String mArtistName;
    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private String mNowPlayingUrl = "";
    private boolean mIsPrepared = false;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private Intent mNotificationIntent;

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

        mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationBuilder = new NotificationCompat.Builder(this);

        if (this.getResources().getBoolean(R.bool.wide_layout)) {
            mNotificationIntent = new Intent(this, MainActivity.class);
        } else {
            mNotificationIntent = new Intent(this, PlayerActivity.class);
            mNotificationIntent.putExtra(PlayerFragment.ARTIST_NAME_KEY, mArtistName)
                    .putExtra(PlayerFragment.TRACK_INFO_KEY, mTrackInfo)
                    .putExtra(PlayerFragment.TRACK_POSITION_KEY, mTrackPosition);
            mNotificationIntent.setAction(PlayerFragment.ACTION_LAUNCH_FROM_NOTIFICATION);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(pendingIntent);
        mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "Service started");

        mArtistName = intent.getStringExtra(PlayerFragment.ARTIST_NAME_KEY);
        mTrackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
        mTrackPosition = intent.getIntExtra(PlayerFragment.TRACK_POSITION_KEY, -1);

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(mTrackInfo.getTrackNames().get(mTrackPosition));

        Notification notification = mNotificationBuilder.build();

        this.startForeground(MUSIC_PLAYER_NOTIFICATION_ID, notification);

        Target target = new Target() {
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mNotificationBuilder.setLargeIcon(bitmap);
                mNotificationManager.notify(MUSIC_PLAYER_NOTIFICATION_ID, mNotificationBuilder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }
        };

        Picasso.with(this).load(mTrackInfo.getMediumThumbnails().get(mTrackPosition)).into(target);

        return START_STICKY;
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

        if (!mIsRecoveringFromError) {
            playMedia();
        } else {
            mIsRecoveringFromError = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        //TODO: temp code to dispose itself
        //stopSelf();
    }

    private void resetMediaOnError() {
        mIsRecoveringFromError = true;
        resetMedia();
        playTrack(mTrackInfo.getTrackPreviewUrls().get(mTrackPosition));
    }

    private void resetMedia() {
        mIsPrepared = false;
        mMediaPlayer.reset();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Utility.displayToast(this, getString(R.string.error_playback));
        resetMediaOnError();
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
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
                    String intentTrackUrl = mTrackInfo.getTrackPreviewUrls().get(mTrackPosition);

                    if (mNowPlayingUrl.equals(intentTrackUrl) && mIsPrepared) {
                        playMedia();
                    } else {
                        if (mMediaPlayer.isPlaying() || mIsPrepared)
                            resetMedia();

                        mNowPlayingUrl = intentTrackUrl;
                        playTrack(mNowPlayingUrl);
                    }
                }
            }
        }
    }
}
