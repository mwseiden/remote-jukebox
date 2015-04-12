package com.theducksparadise.jukebox;

import com.theducksparadise.jukebox.domain.Song;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class Jukebox extends Activity {
    public static final int UPDATE_QUEUE_MESSAGE = 412391221;
    public static final int UPDATE_SLIDER_MESSAGE = 412392221;

    private static final int SLIDER_UPDATE_TIME = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler handler;

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

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        startActivity(intent);

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
                MusicDatabase.getInstance(getApplicationContext()).deleteSavedQueue();
                return true;
            case 1:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SplashActivity.isQueueLoaded()) updateNowPlaying();
    }

    public void updateNowPlaying() {
        MusicDatabase.getInstance(getApplicationContext()).saveQueue(JukeboxMedia.getInstance().getCurrentSong(), JukeboxMedia.getInstance().getQueue());

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
            nextButton.setBackgroundResource(R.drawable.skip);
        } else {
            titleTextView.setText("");
            albumTextView.setText("");
            artistTextView.setText("");
            queueTextView.setText("");
            nextButton.setBackgroundResource(R.drawable.next_unavailable_button);
            Song randomSong = MusicDatabase.getInstance(getApplicationContext()).getRandomSong();
            if (randomSong != null) JukeboxMedia.getInstance().addToQueue(randomSong.getSongsForQueue());
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

        for (Song song: JukeboxMedia.getInstance().getQueue()) {
            queueText += song.getName() + "\n";
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
            if (msg.what == UPDATE_QUEUE_MESSAGE) {
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
