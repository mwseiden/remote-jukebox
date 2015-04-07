package com.theducksparadise.jukebox.domain;

import java.util.Collection;

public abstract class NamedItem {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract Collection<Song> getSongsForQueue();

    @Override
    public String toString() {
        return name;
    }
}
