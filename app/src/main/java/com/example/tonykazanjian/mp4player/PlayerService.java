package com.example.tonykazanjian.mp4player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.MediaController;

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
    public static final String EXTRA_IS_UI_PAUSED = "EXTRA_IS_UI_PAUSED";

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
//        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PlayerActivity.initializeMedia();

        startForeground(NOTIFY_ID, getNotificationBuilder().build());

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
                    .setContentIntent(getRegularUIPendingIntent());
//                    .addAction(R.drawable.pause, getString(R.string.notification_pause_action), getPausePendingIntent())
//                    .setContentTitle(mCurrentMovement.getMovementType())
//                    .setContentText(getString(R.string.songbyartist, currentSong.getTitle(), currentSong.getArtist()))
//                    .setContentIntent(getRegularUIPendingIntent());
            mNotificationManager.notify(NOTIFY_ID, mNotificationBuilder.build());
        }
        return  mNotificationBuilder;
    }

    private PendingIntent getRegularUIPendingIntent(){
        Intent videoIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        videoIntent.putExtra(EXTRA_IS_UI_PAUSED,false);
        videoIntent.putExtra(PlayerActivity.EXTRA_REBIND_PLAYER_SERVICE, true);
        videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mRegularUIPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, videoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mRegularUIPendingIntent;
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService () {
            return PlayerService.this;
        }
    }
}
