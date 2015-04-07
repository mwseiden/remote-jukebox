package com.theducksparadise.jukebox;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class RefreshDatabaseWaitActivity extends Activity {
    public static final String PATH = "asyncTask";
    public static final int UPDATE_MESSAGE = 412391220;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler handler = new ProgressHandler(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_wait);

        setDoingSomethingText("Reloading Database");
        setProgressText("Initializing...");

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            RefreshDatabaseAsync task = new RefreshDatabaseAsync();
            task.setActivity(this);
            task.setHandler(handler);
            task.setPath(extras.getString(PATH));
            task.setContext(getApplicationContext());

            task.execute();
        } else {
            finish();
        }
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

    private static class ProgressHandler extends Handler {
        private RefreshDatabaseWaitActivity activity;

        public ProgressHandler(RefreshDatabaseWaitActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_MESSAGE){
                activity.setProgressText(msg.obj.toString());
            }
            super.handleMessage(msg);
        }

    }
}
