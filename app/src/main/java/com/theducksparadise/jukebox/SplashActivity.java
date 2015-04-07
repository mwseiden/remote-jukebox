package com.theducksparadise.jukebox;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.theducksparadise.jukebox.domain.Song;

public class SplashActivity extends Activity {
    private static boolean queueLoaded = false;

    public static final int FINISH_MESSAGE = 412394420;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_wait);

        setDoingSomethingText("Jukebox");
        setProgressText("Initializing...");

        handler = new FinishedHandler(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicDatabase musicDatabase = MusicDatabase.getInstance(getApplicationContext());

                if (!queueLoaded) {
                    JukeboxMedia.getInstance().addToQueue(musicDatabase.loadQueue());
                    queueLoaded = true;
                }

                Message message = new Message();

                message.what = SplashActivity.FINISH_MESSAGE;

                handler.sendMessage(message);
            }
        }).start();
    }

    private void setDoingSomethingText(String text) {
        TextView myTextView = (TextView)findViewById(R.id.doingSomethingText);
        myTextView.setText(text);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/LCD.otf");
        myTextView.setTextSize(40.0f);
        myTextView.setTypeface(typeFace);
    }

    private void setProgressText(String text) {
        TextView myTextView = (TextView)findViewById(R.id.progressText);
        myTextView.setText(text);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/LCD.otf");
        myTextView.setTextSize(16.0f);
        myTextView.setTypeface(typeFace);
    }

    private static class FinishedHandler extends Handler {
        private SplashActivity activity;

        public FinishedHandler(SplashActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FINISH_MESSAGE) {
                activity.finish();
            }
            super.handleMessage(msg);
        }

    }

}
