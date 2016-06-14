package com.example.tonykazanjian.mp4player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by tonykazanjian on 6/10/16.
 */
public class PlayerActivity extends Activity implements MediaController.MediaPlayerControl {

    public VideoView mVideoView;
    private static DisplayMetrics dm;
    private PlayerService mPlayerService;
    private boolean isServiceBound = false;
    public boolean mRebindingService = false;

    public String mVideoString = "http://www.ebookfrenzy.com/android_book/movie.mp4";

    private boolean isPaused = false;
    public static final String EXTRA_REBIND_PLAYER_SERVICE = "EXTRA_REBIND_PLAYER_SERVICE";

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();

            if (mRebindingService) {
                onRebindMusicService();
            } else {
                PlayerService.startVideos(PlayerActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Bundle extras = getIntent().getExtras();

        if (extras != null){
            mRebindingService = extras.getBoolean(EXTRA_REBIND_PLAYER_SERVICE, false);

        }
        mVideoView = (VideoView) findViewById(R.id.video_view);
        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        mVideoView.setMinimumHeight(height);
        mVideoView.setMinimumWidth(width);

    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindPlayerService();

    }

    @Override
    protected void onPause(){
        super.onPause();
        isPaused=true;
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        doUnbindPlayerService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mVideoReceiver);
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(isPaused){
            isPaused=false;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mVideoReceiver, new IntentFilter(PlayerService.PLAYER_BROADCAST_EVENT));
    }

    private void doBindPlayerService() {
        if (!isServiceBound){
            isServiceBound = bindService(new Intent(this, PlayerService.class),
                    mPlayerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void initializeMedia(){
        mVideoView.setVideoPath(mVideoString);
        mVideoView.start();
    }

    private void doUnbindPlayerService() {
        if (isServiceBound) {
            isServiceBound = false;
            unbindService(mPlayerServiceConnection);
            mPlayerService = null;
        }
    }

    private void onRebindMusicService() {
        if (isServiceBound && mPlayerService != null) {

            // this check needs to be here to ensure the play/pause state is restored when you have
            // the app waiting in recents/multitasking, and you use the service to toggle the play/pause state
            // e.g. Start playing the video with the app open, then hit multitasking. While multitasking is still open,
            // pull down the notification shade and pause the class. Now touch the app in multitasking, and resume it.
            // The pause state should be showing in the app UI.
            if (mPlayerService.isVideoPlaying()) {
                onVideoPlay();
            } else {
                onVideoPause();
            }
        }
    }

    public void onVideoPlay(){
        mVideoView.resume();
    }

    public void onVideoPause(){
        mVideoView.pause();
    }

    @Override
    public void start() {
        mPlayerService.go();
    }

    @Override
    public void pause() {
        mPlayerService.pausePlayer();
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int i) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /***** BROADCAST RECEIVERS *****/

    private BroadcastReceiver mVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(PlayerService.EXTRA_PLAYER_BROADCAST_MSG);

            switch (message) {
                case PlayerService.PLAYER_START_MSG:
                    initializeMedia();
                    break;
                case PlayerService.PLAYER_NOTIFICATION_PAUSE_MSG:
                    onVideoPause();
                    break;
                case PlayerService.PLAYER_NOTIFICATION_PLAY_MSG:
                    onVideoPlay();
                    break;
            }
        }
    };
}
