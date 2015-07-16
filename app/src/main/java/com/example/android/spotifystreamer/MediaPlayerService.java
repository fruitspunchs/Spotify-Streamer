package com.example.android.spotifystreamer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;


/**
 * Service that streams music given a url
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    public static final String ACTION_STOP = "STOP";

    public static final String EVENT_MEDIA = "";


    private static final int MUSIC_PLAYER_NOTIFICATION_ID = 100;
    private static String LOG_TAG;
    private boolean mIsRecoveringFromError = false;
    private String mArtistName;
    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private String mNowPlayingUrl = "";
    private boolean mIsPrepared = false;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationCompat.MediaStyle mMediaStyle;
    private NotificationManager mNotificationManager;

    private Target mTarget = new Target() {
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
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mMediaStyle = new NotificationCompat.MediaStyle();

        Intent deleteIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        deleteIntent.setAction(ACTION_STOP);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(getApplicationContext(), 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent selectIntent;
        if (getApplication().getResources().getBoolean(R.bool.wide_layout)) {
            selectIntent = new Intent(this, MainActivity.class);
        } else {
            selectIntent = new Intent(this, PlayerActivity.class);
            selectIntent.putExtra(PlayerFragment.ARTIST_NAME_KEY, mArtistName)
                    .putExtra(PlayerFragment.TRACK_INFO_KEY, mTrackInfo)
                    .putExtra(PlayerFragment.TRACK_POSITION_KEY, mTrackPosition);
            selectIntent.setAction(PlayerFragment.ACTION_LAUNCH_FROM_NOTIFICATION);
        }
        PendingIntent pendingSelectIntent = PendingIntent.getActivity(getApplicationContext(), 0, selectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notification_title))
                .setContentIntent(pendingSelectIntent)
                .setDeleteIntent(pendingDeleteIntent)
                .setStyle(mMediaStyle);

        initMediaSessions();
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(NotificationCompat.Action action) {

        mNotificationBuilder.setContentText(mTrackInfo.getTrackNames().get(mTrackPosition));

        mNotificationBuilder.mActions.clear();

        mNotificationBuilder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        mNotificationBuilder.addAction(action);
        mNotificationBuilder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        mMediaStyle.setShowActionsInCompactView(0, 1, 2);

        Picasso.with(this).load(mTrackInfo.getMediumThumbnails().get(mTrackPosition)).into(mTarget);

        mNotificationManager.notify(MUSIC_PLAYER_NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void sendMessage(String action) {
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = action;
        mServiceHandler.sendMessage(msg);
    }

    private void initMediaSessions() {

        //TODO: test on pre lollipop device
        mSession = new MediaSessionCompat(getApplicationContext(), "Media Player Session", new ComponentName(this, MediaPlayerService.class), null);

        try {
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
            stopSelf();
        }

        mSession.setCallback(new MediaSessionCompat.Callback() {
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     Log.d("MediaPlayerService", "onPlay");

                                     sendMessage(ACTION_PLAY);

                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.d("MediaPlayerService", "onPause");

                                     sendMessage(ACTION_PAUSE);

                                     buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                 }


                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     Log.d("MediaPlayerService", "onSkipToNext");

                                     sendMessage(ACTION_PLAY);

                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     Log.d("MediaPlayerService", "onSkipToPrevious");

                                     sendMessage(ACTION_PLAY);

                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     Log.d("MediaPlayerService", "onStop");
                                     stopSelf();
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                 }

                                 @Override
                                 public void onSetRating(RatingCompat rating) {
                                     super.onSetRating(rating);
                                 }
                             }
        );
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSession.release();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Service started");

        if (intent.hasExtra(PlayerFragment.ARTIST_NAME_KEY) && intent.hasExtra(PlayerFragment.TRACK_INFO_KEY) && intent.hasExtra(PlayerFragment.TRACK_POSITION_KEY)) {
            mArtistName = intent.getStringExtra(PlayerFragment.ARTIST_NAME_KEY);
            mTrackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
            mTrackPosition = intent.getIntExtra(PlayerFragment.TRACK_POSITION_KEY, -1);
        }

        if (intent.getAction().equals(ACTION_NEXT)) {
            mTrackPosition = mTrackInfo.getNextTrackIndex(mTrackPosition);
        } else if (intent.getAction().equals(ACTION_PREVIOUS)) {
            mTrackPosition = mTrackInfo.getPreviousTrackIndex(mTrackPosition);
        }

        handleIntent(intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Service destroyed");
        mNotificationManager.cancel(MUSIC_PLAYER_NOTIFICATION_ID);

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mSession != null) {
            mSession.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void loadTrack(String urlString) {
        if (Utility.isNetworkConnected(this)) {
            try {
                mIsPrepared = false;
                mMediaPlayer.setDataSource(urlString);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                Utility.displayToast(this, getString(R.string.error_unable_to_play_track));
            }
        }
    }

    private void playMedia() {
        mMediaPlayer.start();
    }

    private void pauseMedia() {
        mMediaPlayer.pause();
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
        buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
    }

    private void resetMediaOnError() {
        mIsRecoveringFromError = true;
        resetMedia();
        loadTrack(mTrackInfo.getTrackPreviewUrls().get(mTrackPosition));
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
        Log.d(LOG_TAG, "onTaskRemoved");
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
                String action = (String) msg.obj;

                if (action.equals(ACTION_PLAY)) {
                    String intentTrackUrl = mTrackInfo.getTrackPreviewUrls().get(mTrackPosition);

                    if (mNowPlayingUrl.equals(intentTrackUrl) && mIsPrepared) {
                        playMedia();
                    } else {
                        if (mMediaPlayer.isPlaying() || mIsPrepared)
                            resetMedia();

                        mNowPlayingUrl = intentTrackUrl;
                        loadTrack(mNowPlayingUrl);
                    }
                } else if (action.equals(ACTION_PAUSE)) {
                    pauseMedia();
                }
            }
        }
    }
}
