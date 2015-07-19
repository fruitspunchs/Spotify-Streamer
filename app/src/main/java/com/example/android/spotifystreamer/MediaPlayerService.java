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
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.Serializable;

//TODO: handle AUDIO_BECOMING_NOISY
//TODO: handle audio focus
//TODO: notify player of load amount

/**
 * Streams tracks and shows a notification with music player controls
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_SEEK = "ACTION_SEEK";
    public static final String TRACK_SEEK_POSITION_KEY = "TRACK_SEEK_POSITION_KEY";

    public static final String MEDIA_EVENT = "MEDIA_EVENT";
    public static final String MEDIA_EVENT_KEY = "MEDIA_EVENT_KEY";

    public static final String MEDIA_EVENT_BUFFERING_PROGRESS = "MEDIA_EVENT_BUFFERING_PROGRESS";
    public static final String BUFFER_PROGRESS_KEY = "BUFFER_PROGRESS_KEY";

    public static final String MEDIA_EVENT_TRACK_PROGRESS = "MEDIA_EVENT_TRACK_PROGRESS";
    public static final String TRACK_PROGRESS_KEY = "TRACK_PROGRESS_KEY";

    public static final String MEDIA_EVENT_PLAYING = "MEDIA_EVENT_PLAYING";
    public static final String TRACK_URL_KEY = "TRACK_URL_KEY";

    public static final String MEDIA_EVENT_NOT_PLAYING = "MEDIA_EVENT_NOT_PLAYING";
    public static final String MEDIA_EVENT_REQUEST_NOW_PLAYING = "MEDIA_EVENT_REQUEST_NOW_PLAYING";
    public static final String MEDIA_EVENT_REPLY_NOW_PLAYING = "MEDIA_EVENT_REPLY_NOW_PLAYING";
    public static final String MEDIA_EVENT_IS_TRACK_LOADED = "MEDIA_EVENT_IS_TRACK_LOADED";

    public static final String IS_PLAYING_KEY = "IS_PLAYING_KEY";

    private static final int MUSIC_PLAYER_NOTIFICATION_ID = 100;

    private static String LOG_TAG;
    private final int mTrackProgressUpdateDelay = 100; //milliseconds
    private int mTrackProgress;
    private String mArtistName;
    private TrackInfo mTrackInfo;
    private int mTrackPosition;
    private String mNowPlayingUrl = "";
    private boolean mIsRecoveringFromError = false;
    private boolean mIsPrepared = false;
    private boolean mIsPreparing = false;
    private ServiceHandler mServiceHandler;
    private Handler mTrackProgressUpdateHandler;

    private MediaPlayer mMediaPlayer;
    private int mBufferPercent = 0;

    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationCompat.MediaStyle mMediaStyle;
    private NotificationManager mNotificationManager;

    private WifiManager.WifiLock mWifiLock;


    private Target mImageTarget = new Target() {
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

        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mMediaStyle = new NotificationCompat.MediaStyle();

        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notification_title))
                .setShowWhen(false)
                .setStyle(mMediaStyle);

        initMediaSessions();

        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "Spotify Streamer");
        mWifiLock.setReferenceCounted(false);

        mTrackProgressUpdateHandler = new Handler();
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY:
                mController.getTransportControls().play();
                break;
            case ACTION_PAUSE:
                mController.getTransportControls().pause();
                break;
            case ACTION_PREVIOUS:
                mTrackPosition = mTrackInfo.getPreviousTrackIndex(mTrackPosition);
                mController.getTransportControls().skipToPrevious();
                break;
            case ACTION_NEXT:
                mTrackPosition = mTrackInfo.getNextTrackIndex(mTrackPosition);
                mController.getTransportControls().skipToNext();
                break;
            case ACTION_STOP:
                mController.getTransportControls().stop();
                break;
            case ACTION_SEEK:
                mController.getTransportControls().seekTo(intent.getIntExtra(TRACK_SEEK_POSITION_KEY, 0));
                break;
            case MEDIA_EVENT_REQUEST_NOW_PLAYING:
                sendMessageToServiceHandler(MEDIA_EVENT_REQUEST_NOW_PLAYING);
                break;
            case MEDIA_EVENT_IS_TRACK_LOADED:
                sendMessageToServiceHandler(MEDIA_EVENT_IS_TRACK_LOADED);
                break;
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private boolean isMediaPlaying() {
        if (mIsPrepared && mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) return true;
        }

        return false;
    }

    private void buildNotification(NotificationCompat.Action action) {
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
                    .putExtra(PlayerFragment.TRACK_INDEX_KEY, mTrackPosition)
                    .putExtra(BUFFER_PROGRESS_KEY, mBufferPercent)
                    .putExtra(IS_PLAYING_KEY, isMediaPlaying())
                    .putExtra(TRACK_PROGRESS_KEY, mTrackProgress);
            selectIntent.setAction(PlayerFragment.ACTION_LAUNCH_ONLY);
        }
        PendingIntent pendingSelectIntent = PendingIntent.getActivity(getApplicationContext(), 0, selectIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(pendingSelectIntent)
                .setDeleteIntent(pendingDeleteIntent);

        mNotificationBuilder.setContentText(mTrackInfo.getTrackNames().get(mTrackPosition));

        mNotificationBuilder.mActions.clear();

        mNotificationBuilder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        mNotificationBuilder.addAction(action);
        mNotificationBuilder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        mMediaStyle.setShowActionsInCompactView(0, 1, 2);

        Picasso.with(this).load(mTrackInfo.getMediumThumbnails().get(mTrackPosition)).into(mImageTarget);

        mNotificationManager.notify(MUSIC_PLAYER_NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void sendMessageToServiceHandler(String action) {
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = action;
        mServiceHandler.sendMessage(msg);
    }

    private void sendMessageToServiceHandler(String action, int value) {
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = action;
        msg.arg1 = value;
        mServiceHandler.sendMessage(msg);
    }

    private void initMediaSessions() {

        //TODO: Test on pre lollipop device
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
                                     if (!mIsPreparing) {
                                         Log.d(LOG_TAG, "onPlay");
                                         sendMessageToServiceHandler(ACTION_PLAY);

                                         buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                         broadcastMessage(ACTION_PLAY);
                                     }
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.d(LOG_TAG, "onPause");

                                     sendMessageToServiceHandler(ACTION_PAUSE);

                                     buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                     broadcastMessage(ACTION_PAUSE);
                                 }


                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     Log.d(LOG_TAG, "onSkipToNext");

                                     broadcastMessage(ACTION_NEXT, PlayerFragment.TRACK_INDEX_KEY, mTrackPosition);
                                     sendMessageToServiceHandler(ACTION_NEXT);

                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     Log.d(LOG_TAG, "onSkipToPrevious");

                                     broadcastMessage(ACTION_PREVIOUS, PlayerFragment.TRACK_INDEX_KEY, mTrackPosition);
                                     sendMessageToServiceHandler(ACTION_PREVIOUS);

                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     Log.d(LOG_TAG, "onStop");
                                     broadcastMessage(ACTION_STOP);
                                     broadcastMessage(MEDIA_EVENT_NOT_PLAYING);
                                     mWifiLock.release();
                                     stopSelf();
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                     Log.d(LOG_TAG, "onSeekTo");
                                     sendMessageToServiceHandler(ACTION_SEEK, (int) pos);

                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                     broadcastMessage(ACTION_PLAY);

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

        if (intent != null) {
            if (intent.hasExtra(PlayerFragment.ARTIST_NAME_KEY) && intent.hasExtra(PlayerFragment.TRACK_INFO_KEY) && intent.hasExtra(PlayerFragment.TRACK_INDEX_KEY)) {
                mArtistName = intent.getStringExtra(PlayerFragment.ARTIST_NAME_KEY);
                mTrackInfo = intent.getParcelableExtra(PlayerFragment.TRACK_INFO_KEY);
                mTrackPosition = intent.getIntExtra(PlayerFragment.TRACK_INDEX_KEY, -1);
            }
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

        if (mWifiLock != null) {
            mWifiLock.release();
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
                mIsPreparing = true;
                mMediaPlayer.setDataSource(urlString);
                mWifiLock.acquire();
                mMediaPlayer.prepareAsync();
                broadcastMessage(MEDIA_EVENT_PLAYING, TRACK_URL_KEY, mNowPlayingUrl);

            } catch (IOException e) {
                Utility.displayToast(this, getString(R.string.error_unable_to_play_track));
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPrepared = true;
        mIsPreparing = false;

        if (!mIsRecoveringFromError) {
            playMedia();
        } else {
            mIsRecoveringFromError = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mWifiLock.release();
        buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
        broadcastMessage(ACTION_PAUSE);
    }

    private void playMedia() {
        mMediaPlayer.start();

        broadcastMessage(MEDIA_EVENT_BUFFERING_PROGRESS, BUFFER_PROGRESS_KEY, mBufferPercent);

        mTrackProgressUpdateHandler.postDelayed(new Runnable() {
            public void run() {
                if (mIsPrepared && mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mTrackProgressUpdateHandler.postDelayed(this, mTrackProgressUpdateDelay);
                        int duration = mMediaPlayer.getDuration();
                        float progress = ((float) mMediaPlayer.getCurrentPosition() / (float) duration) * 100;
                        mTrackProgress = (int) progress;
                        broadcastMessage(MEDIA_EVENT_TRACK_PROGRESS, TRACK_PROGRESS_KEY, mTrackProgress);
                    }
                }
            }
        }, mTrackProgressUpdateDelay);
    }

    private void pauseMedia() {
        mWifiLock.release();

        if (mIsPrepared) {
            mMediaPlayer.pause();
        } else {
            resetMedia();
        }
    }

    private void resetMediaOnError() {
        mIsRecoveringFromError = true;
        resetMedia();
        loadTrack(mTrackInfo.getTrackPreviewUrls().get(mTrackPosition));
        broadcastMessage(ACTION_PAUSE);
    }

    private void resetMedia() {
        mIsPrepared = false;
        mIsPreparing = false;
        mBufferPercent = 0;
        mTrackProgress = 0;
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

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferPercent = percent;
        broadcastMessage(MEDIA_EVENT_BUFFERING_PROGRESS, BUFFER_PROGRESS_KEY, percent);
    }

    private void broadcastMessage(String message) {
        Log.d(LOG_TAG, "Broadcasting message: " + message);
        Intent intent = new Intent(MEDIA_EVENT);

        intent.putExtra(MEDIA_EVENT_KEY, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private <T extends Serializable> void broadcastMessage(String message, String key, T value) {
        if (!message.equals(MediaPlayerService.MEDIA_EVENT_TRACK_PROGRESS)) {
            Log.d(LOG_TAG, "Broadcasting message: " + message);
        }

        Intent intent = new Intent(MEDIA_EVENT);

        intent.putExtra(MEDIA_EVENT_KEY, message);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastPlayerState() {
        Log.d(LOG_TAG, "Broadcasting player info");

        Intent intent = new Intent(MEDIA_EVENT);

        intent.putExtra(PlayerFragment.ARTIST_NAME_KEY, mArtistName)
                .putExtra(PlayerFragment.TRACK_INFO_KEY, mTrackInfo)
                .putExtra(PlayerFragment.TRACK_INDEX_KEY, mTrackPosition)
                .putExtra(BUFFER_PROGRESS_KEY, mBufferPercent)
                .putExtra(IS_PLAYING_KEY, isMediaPlaying())
                .putExtra(TRACK_PROGRESS_KEY, mTrackProgress);

        intent.putExtra(MEDIA_EVENT_KEY, MEDIA_EVENT_REPLY_NOW_PLAYING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
                String intentTrackUrl;

                switch (action) {
                    case ACTION_PLAY:
                        intentTrackUrl = mTrackInfo.getTrackPreviewUrls().get(mTrackPosition);
                        if (mNowPlayingUrl.equals(intentTrackUrl) && mIsPrepared) {
                            playMedia();
                        } else {
                            if (mMediaPlayer.isPlaying() || mIsPrepared)
                                resetMedia();

                            mNowPlayingUrl = intentTrackUrl;
                            loadTrack(mNowPlayingUrl);
                        }
                        break;
                    case ACTION_PAUSE:
                        pauseMedia();
                        break;
                    case ACTION_NEXT:
                    case ACTION_PREVIOUS:
                        intentTrackUrl = mTrackInfo.getTrackPreviewUrls().get(mTrackPosition);
                        resetMedia();
                        mNowPlayingUrl = intentTrackUrl;
                        loadTrack(mNowPlayingUrl);
                        break;
                    case ACTION_SEEK:
                        if (mIsPrepared) {
                            int duration = mMediaPlayer.getDuration();
                            int seekPercentage = msg.arg1;
                            int seekPosition = (int) (duration * seekPercentage / 100.0f);
                            mMediaPlayer.seekTo(seekPosition);
                            playMedia();
                        }
                        break;
                    case MEDIA_EVENT_REQUEST_NOW_PLAYING:
                        broadcastPlayerState();
                        break;
                    case MEDIA_EVENT_IS_TRACK_LOADED:
                        if (!mNowPlayingUrl.equals("")) {
                            broadcastMessage(MEDIA_EVENT_PLAYING, TRACK_URL_KEY, mNowPlayingUrl);
                        } else {
                            broadcastMessage(MEDIA_EVENT_NOT_PLAYING);
                        }
                        break;
                }
            }
        }
    }
}

