package com.theducksparadise.jukebox;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

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

    private Handler handler = null;

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
        for (NamedItem item: items) {
            queue.addAll(item.getSongsForQueue());
        }

        if (currentPlayer == null) queueNext();
    }

    public void clearQueue() {
        queue.clear();
    }

    public void skip() {
        queueNext();
    }

    public void togglePlay() {
        if (currentPlayer != null) {
            if (currentPlayer.isPlaying()) {
                currentPlayer.pause();
            } else {
                currentPlayer.start();
            }
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void queueNext() {
        if (currentPlayer != null) {
            currentPlayer.release();
            currentPlayer = null;
            currentSong = null;
        }

        boolean success = false;
        while (queue.size() > 0 && !success) {
            try {
                currentSong = (Song) queue.poll();
                currentPlayer = new MediaPlayer();
                currentPlayer.setDataSource(currentSong.getFileName());
                currentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        queueNext();
                    }
                });
                currentPlayer.prepare();
                currentPlayer.start();
                signalRefresh();
                success = true;
            } catch (IOException e) {
                success = false;
            }
        }
    }

    private void signalRefresh() {
        if (handler != null) {
            Message message = new Message();

            message.what = Jukebox.UPDATE_QUEUE_MESSAGE;

            handler.sendMessage(message);

        }
    }
}
