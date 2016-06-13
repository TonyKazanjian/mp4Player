package com.example.tonykazanjian.mp4player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;

/**
 * Created by tonykazanjian on 6/13/16.
 */
public class PlayerController extends MediaController {
    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public PlayerController(Context context) {
        super(context);
    }
}
