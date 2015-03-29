package com.theducksparadise.jukebox.domain;

import java.util.ArrayList;
import java.util.List;

public class Album {

    private int id;
    private Artist artist;
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

        return name.equals(album.name) && album.artist.getName().equals(artist.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
