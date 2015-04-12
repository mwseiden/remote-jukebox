package com.theducksparadise.jukebox.domain;

import java.util.Collection;

public class Tag extends NamedItem {

    @Override
    public Collection<Song> getSongsForQueue() {
        return null;
    }
}
