package com.theducksparadise.jukebox;

import com.theducksparadise.jukebox.domain.NamedItem;
import com.theducksparadise.jukebox.domain.Song;
import com.theducksparadise.jukebox.util.SystemUiHider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Jukebox extends Activity {
    public static final int UPDATE_QUEUE_MESSAGE = 412391221;
    public static final int UPDATE_SLIDER_MESSAGE = 412392221;

    private static final int SLIDER_UPDATE_TIME = 200;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_jukebox);

        setMultiSegmentFont(R.id.artistText);
        setMultiSegmentFont(R.id.albumText);
        setMultiSegmentFont(R.id.titleText);
        setMultiSegmentFont(R.id.queueText, 20.0f);

        ImageButton libraryButton = (ImageButton)findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicListActivity.class);
                startActivity(intent);
            }
        });

        ImageButton nextButton = (ImageButton)findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JukeboxMedia.getInstance().skip();
            }
        });

        ImageButton playButton = (ImageButton)findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JukeboxMedia.getInstance().togglePlay();
                updatePlayButton();
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                JukeboxMedia.getInstance().setProgress(seekBar.getProgress());
            }
        });

        handler = new QueueHandler(this);
        JukeboxMedia.getInstance().setHandler(handler);
        handler.postDelayed(new SliderThread(handler), SLIDER_UPDATE_TIME);

        //final View controlsView = findViewById(R.id.fullscreen_content_controls);
        //final View contentView = findViewById(R.id.fullscreen_content_controls);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        //mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        //mSystemUiHider.setup();
        /*
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.playButton).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.pauseButton).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.libraryButton).setOnTouchListener(mDelayHideTouchListener);
        */
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setMultiSegmentFont(int id) {
        setMultiSegmentFont(id, 40.0f);
    }

    private void setMultiSegmentFont(int id, float size) {
        TextView myTextView = (TextView)findViewById(id);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/LCD.otf");
        myTextView.setTextSize(size);
        myTextView.setTypeface(typeFace);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Clear Queue");
        menu.add(Menu.NONE, 1, 1, "Settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                JukeboxMedia.getInstance().clearQueue();
                return true;
            case 1:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    public void updateNowPlaying() {
        TextView titleTextView = (TextView) findViewById(R.id.titleText);
        TextView artistTextView = (TextView) findViewById(R.id.artistText);
        TextView albumTextView = (TextView) findViewById(R.id.albumText);
        TextView queueTextView = (TextView) findViewById(R.id.queueText);
        ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);

        Song song = JukeboxMedia.getInstance().getCurrentSong();

        if (song != null) {
            titleTextView.setText(song.getName());
            albumTextView.setText(song.getAlbum().getName());
            artistTextView.setText(song.getAlbum().getArtist().getName());
            queueTextView.setText(getQueueText());
            if (JukeboxMedia.getInstance().getQueue().isEmpty()) {
                nextButton.setBackgroundResource(R.drawable.next_unavailable_button);
            } else {
                nextButton.setBackgroundResource(R.drawable.skip);
            }
        } else {
            titleTextView.setText("");
            albumTextView.setText("");
            artistTextView.setText("");
            queueTextView.setText("");
            nextButton.setBackgroundResource(R.drawable.next_unavailable_button);
        }

        updatePlayButton();
        updateSlider();
    }

    private void updatePlayButton() {
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);

        if (JukeboxMedia.getInstance().getCurrentSong() == null) {
            playButton.setBackgroundResource(R.drawable.play_button);
        } else if (JukeboxMedia.getInstance().isPlaying()) {
            playButton.setBackgroundResource(R.drawable.pause);
        } else {
            playButton.setBackgroundResource(R.drawable.play);
        }
    }

    private String getQueueText() {
        String queueText = "";

        for (NamedItem item: JukeboxMedia.getInstance().getQueue()) {
            queueText += item.getName() + "\n";
        }

        return queueText;
    }

    private void updateSlider() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);

        Integer duration = JukeboxMedia.getInstance().getDuration();
        Integer progress = JukeboxMedia.getInstance().getProgress();

        if (duration != null && progress != null) {
            seekBar.setVisibility(View.VISIBLE);
            seekBar.setMax(duration);
            seekBar.setProgress(progress);
        } else {
            seekBar.setVisibility(View.INVISIBLE);
        }
    }

    private static class QueueHandler extends Handler {
        private Jukebox activity;

        public QueueHandler(Jukebox activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_QUEUE_MESSAGE){
                activity.updateNowPlaying();
            } if (msg.what == UPDATE_SLIDER_MESSAGE) {
                activity.updateSlider();
            }

            super.handleMessage(msg);
        }
    }

    private class SliderThread implements Runnable {

        Handler handler;

        public SliderThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                if (JukeboxMedia.getInstance().getCurrentSong() != null) {
                    Message message = new Message();

                    message.what = UPDATE_SLIDER_MESSAGE;

                    handler.sendMessage(message);
                }
            } finally {
                handler.postDelayed(this, SLIDER_UPDATE_TIME);
            }
        }
    }
}
