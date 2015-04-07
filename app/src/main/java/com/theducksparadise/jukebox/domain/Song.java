package com.theducksparadise.jukebox.domain;

import java.util.ArrayList;
import java.util.Collection;

public class Song extends NamedItem {

    private int id;
    private int sequence;
    private Album album;
    private String fileName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        return sequence == song.sequence &&
                fileName.equals(song.fileName) &&
                getName().equals(song.getName()) &&
                album.getName().equals(song.getAlbum().getName()) &&
                album.getArtist().getName().equals(song.getAlbum().getArtist().getName());

    }

    @Override
    public int hashCode() {
        int result = sequence;
        result = 31 * result + getName().hashCode();
        result = 31 * result + fileName.hashCode();
        return result;
    }

    @Override
    public Collection<Song> getSongsForQueue() {
        Collection<Song> items = new ArrayList<>();
        items.add(this);
        return items;
    }
}
