package com.example.tonykazanjian.mp4player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by tonykazanjian on 6/10/16.
 */
public class PlayerActivity extends Activity {

    public static VideoView mVideoView;
    private static DisplayMetrics dm;
    private PlayerService mPlayerService;
    private boolean isServiceBound = false;

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();

            PlayerService.startVideos(PlayerActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mVideoView = (VideoView) findViewById(R.id.video_view);
//        dm = new DisplayMetrics();
//        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int height = dm.heightPixels;
//        int width = dm.widthPixels;
//        mVideoView.setMinimumHeight(height);
//        mVideoView.setMinimumWidth(width);
    }

    @Override
    protected void onStart() {
        super.onStart();

        doBindPlayerService();

    }

    @Override
    protected void onStop() {
        // Unbind from the service
        doUnbindPlayerService();
        super.onStop();
    }

    private void doBindPlayerService() {
        if (!isServiceBound){
            isServiceBound = bindService(new Intent(this, PlayerService.class),
                    mPlayerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public static void initializeMedia(MediaController mediaController){
        PlayerActivity.mVideoView.setMediaController(mediaController);
        mediaController.setAnchorView(PlayerActivity.mVideoView);
        PlayerActivity.mVideoView.setVideoPath("http://www.ebookfrenzy.com/android_book/movie.mp4");
        PlayerActivity.mVideoView.start();
    }

    private void doUnbindPlayerService() {
        if (isServiceBound) {
            isServiceBound = false;
            unbindService(mPlayerServiceConnection);
            mPlayerService = null;
        }
    }
}
