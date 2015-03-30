package com.theducksparadise.jukebox.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Artist extends NamedItem {

    private int id;

    private List<Album> albums;

    public Artist() {
        albums = new ArrayList<Album>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public Album getAlbum(String name) {
        for (Album album: albums) {
            if (album.getName().equals(name)) return album;
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;

        return getName().equals(artist.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public Collection<NamedItem> getSongsForQueue() {
        Collection<NamedItem> items = new ArrayList<NamedItem>();

        for (Album album: getAlbums()) {
            items.addAll(album.getSongsForQueue());
        }

        return items;
    }
}
