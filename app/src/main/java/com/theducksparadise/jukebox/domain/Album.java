package com.theducksparadise.jukebox.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Album extends NamedItem {

    private int id;
    private Artist artist;

    private List<Song> songs;

    public Album() {
        songs = new ArrayList<Song>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        return getName().equals(album.getName()) && album.artist.getName().equals(artist.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public Collection<NamedItem> getSongsForQueue() {
        Collection<NamedItem> items = new ArrayList<NamedItem>();
        items.addAll(getSongs());
        return items;
    }
}
