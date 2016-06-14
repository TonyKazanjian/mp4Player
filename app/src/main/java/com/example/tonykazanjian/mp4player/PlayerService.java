package com.example.tonykazanjian.mp4player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by tonykazanjian on 6/10/16.
 */
public class PlayerService extends Service  {

    private final IBinder mPlayerBinder = new PlayerBinder();
    private MediaPlayer mPlayer;

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManagerCompat mNotificationManager;
    private PendingIntent mPausePendingIntent;
    private PendingIntent mPlayPendingIntent;
    private PendingIntent mRegularUIPendingIntent;

    private static final int NOTIFY_ID=1;
    public static final String PLAYER_START_MSG = "PLAYER_START";
    public static final String ACTION_START_VIDEO = "com.example.tonykazanjian.mp4player.action.ACTION_START_SONG";
    public static final String ACTION_PLAY = "com.example.tonykazanjian.mp4player.action.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.tonykazanjian.mp4player.action.ACTION_PAUSE";
    public static final String EXTRA_IS_UI_PAUSED = "EXTRA_IS_UI_PAUSED";
    public static final String EXTRA_PLAYER_BROADCAST_MSG = "EXTRA_PLAYER_BROADCAST_MSG";
    public static final String PLAYER_NOTIFICATION_PAUSE_MSG = "PLAYER_NOTIFICATION_PAUSE_MSG";
    public static final String PLAYER_NOTIFICATION_PLAY_MSG = "PLAYER_NOTIFICATION_PLAY_MSG";
    public static final String PLAYER_BROADCAST_EVENT = "PLAYER_BROADCAST_EVENT";

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction().equals(ACTION_START_VIDEO)) {
            sendPlayBroadcastMessage();
            startForeground(NOTIFY_ID, getNotificationBuilder().build());

        }

        else if (intent != null && intent.getAction().equals(ACTION_PAUSE)) {
            sendNotificationPauseBroadcast();
        }

        else if (intent != null && intent.getAction().equals(ACTION_PLAY)) {
            sendNotificationPlayBroadcast();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

    public static Intent startVideos (Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_START_VIDEO);
        context.startService(intent);
        return intent;
    }

    public void pausePlayer(){
        mPlayer.pause();
    }

    public boolean isVideoPlaying() {
        return mPlayer.isPlaying();
    }

    public void go(){
        mPlayer.start();
    }

    /** Notification Methods **/

    private NotificationCompat.Builder getNotificationBuilder() {
        if(mNotificationBuilder != null) {
            return mNotificationBuilder;
        }
        else {

            mNotificationManager = NotificationManagerCompat.from(this);
            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setOngoing(true)
                    .setShowWhen(false)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle("MP4 Player")
                    .addAction(android.R.drawable.ic_media_pause, "Pause", getPausePendingIntent())
                    .setContentIntent(getRegularUIPendingIntent());

            mNotificationManager.notify(NOTIFY_ID, mNotificationBuilder.build());
        }
        return  mNotificationBuilder;
    }

    private void updateNotificationAction(boolean isInPlayState) {
        NotificationCompat.Builder builder = getNotificationBuilder();
        NotificationCompat.Action action = builder.mActions.get(0);

        if(isInPlayState && action.actionIntent != mPausePendingIntent) {
            action.actionIntent = getPausePendingIntent();
            action.icon = android.R.drawable.ic_media_pause;
            action.title = "Pause";
            builder.setContentIntent(getRegularUIPendingIntent());
            mNotificationManager.notify(NOTIFY_ID, builder.build());
        }
        else if(!isInPlayState && action.actionIntent != mPlayPendingIntent) {
            action.actionIntent = getPlayPendingIntent();
            action.icon = android.R.drawable.ic_media_play;
            action.title = "Play";
            builder.setContentIntent(getRegularUIPendingIntent());
            mNotificationManager.notify(NOTIFY_ID, builder.build());
        }
        else {
            // do nothing
        }
    }

    private PendingIntent getRegularUIPendingIntent(){
        Intent videoIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        videoIntent.putExtra(EXTRA_IS_UI_PAUSED,false);
        videoIntent.putExtra(PlayerActivity.EXTRA_REBIND_PLAYER_SERVICE, true);
        videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mRegularUIPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, videoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mRegularUIPendingIntent;
    }

    private PendingIntent getPausePendingIntent() {
        if(mPausePendingIntent != null) return mPausePendingIntent;
        else {
            Intent pauseIntent = new Intent(getApplicationContext(), PlayerService.class);
            pauseIntent.setAction(ACTION_PAUSE);

            mPausePendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            return mPausePendingIntent;
        }
    }

    private PendingIntent getPlayPendingIntent() {
        if(mPlayPendingIntent != null) return mPlayPendingIntent;
        else {
            Intent playIntent = new Intent(getApplicationContext(), PlayerService.class);
            playIntent.setAction(ACTION_PLAY);

            mPlayPendingIntent = PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    playIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            return mPlayPendingIntent;
        }
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService () {
            return PlayerService.this;
        }
    }

    /***** BROADCAST MESSAGES *****/

    private Intent getStandardPlayerBroadcast(String broadcastMessage) {
        Intent intent = new Intent(PLAYER_BROADCAST_EVENT);
        intent.putExtra(EXTRA_PLAYER_BROADCAST_MSG, broadcastMessage);

        return intent;
    }

    private void sendPlayBroadcastMessage(){
        updateNotificationAction(true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                getStandardPlayerBroadcast(PLAYER_START_MSG)
        );
    }

    private void sendNotificationPauseBroadcast() {
        updateNotificationAction(false);

        LocalBroadcastManager.getInstance(this).sendBroadcast(
                getStandardPlayerBroadcast(PLAYER_NOTIFICATION_PAUSE_MSG)
        );
    }

    private void sendNotificationPlayBroadcast() {
        updateNotificationAction(true);

        LocalBroadcastManager.getInstance(this).sendBroadcast(
                getStandardPlayerBroadcast(PLAYER_NOTIFICATION_PLAY_MSG)
        );
    }
}
