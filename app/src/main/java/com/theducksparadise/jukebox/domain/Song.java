package com.theducksparadise.jukebox.domain;

public class Song {

    private int id;
    private int sequence;
    private Album album;
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                name.equals(song.name) &&
                album.getName().equals(song.getAlbum().getName()) &&
                album.getArtist().getName().equals(song.getAlbum().getArtist().getName());

    }

    @Override
    public int hashCode() {
        int result = sequence;
        result = 31 * result + name.hashCode();
        result = 31 * result + fileName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
