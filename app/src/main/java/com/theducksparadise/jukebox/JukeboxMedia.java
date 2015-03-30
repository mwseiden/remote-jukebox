package com.theducksparadise.jukebox;

import android.media.MediaPlayer;

import com.theducksparadise.jukebox.domain.NamedItem;
import com.theducksparadise.jukebox.domain.Song;

import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JukeboxMedia {

    private static volatile JukeboxMedia instance;

    private Queue<NamedItem> queue = new ConcurrentLinkedQueue<NamedItem>();

    private MediaPlayer currentPlayer = null;

    private Song currentSong = null;

    private JukeboxMedia() {
    }

    public static JukeboxMedia getInstance() {
        if (instance == null) {
            synchronized (JukeboxMedia.class) {
                if (instance == null) {
                    instance = new JukeboxMedia();
                }
            }
        }

        return instance;
    }

    public void addToQueue(Collection<NamedItem> items) {
        queue.addAll(items);

        if (currentPlayer == null && !queue.isEmpty()) {
            boolean success = false;
            while (queue.size() > 0 && !success) {
                try {
                    if (currentPlayer != null) currentPlayer.release();
                    currentPlayer = new MediaPlayer();
                    currentPlayer.setDataSource(((Song) queue.poll()).getFileName());
                    currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                            if (queue.size() > 0) {
                                //
                            }
                        }
                    });
                    currentPlayer.start();
                    success = true;
                } catch (IOException e) {
                    success = false;
                }
            }
        }
    }

    public void clearQueue() {
        queue.clear();
    }

    public void skip() {

    }

    public void togglePlay() {

    }

    public Song getCurrentSong() {
        return currentSong;
    }
}
