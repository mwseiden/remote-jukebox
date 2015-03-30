package com.theducksparadise.jukebox;

import android.media.MediaPlayer;

import com.theducksparadise.jukebox.domain.NamedItem;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JukeboxMedia {

    private static volatile JukeboxMedia instance;

    private Queue<NamedItem> queue = new ConcurrentLinkedQueue<NamedItem>();

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
    }
}
