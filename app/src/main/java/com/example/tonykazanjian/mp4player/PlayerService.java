package com.example.tonykazanjian.mp4player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.MediaController;

/**
 * Created by tonykazanjian on 6/10/16.
 */
public class PlayerService extends Service {

    private final IBinder mPlayerBinder = new PlayerBinder();
    MediaController mMediaController;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaController = new MediaController(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PlayerActivity.initializeMedia(mMediaController);

        return START_STICKY;
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

    public class PlayerBinder extends Binder {
        public PlayerService getService () {
            return PlayerService.this;
        }
    }
}
