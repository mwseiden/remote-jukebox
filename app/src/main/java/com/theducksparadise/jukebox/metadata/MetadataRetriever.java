package com.theducksparadise.jukebox.metadata;

import android.media.MediaMetadataRetriever;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;

public class MetadataRetriever {

    private IMusicMetadata myMp3Metadata;
    private MediaMetadataRetriever mmr;

    public MetadataRetriever(File file) {
        MusicMetadataSet myMp3MetadataSet;
        try {
            myMp3MetadataSet = new MyID3().read(file);
            myMp3Metadata = myMp3MetadataSet.getSimplified();
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            // Well that sucks. Ignore it.
        }

        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getAbsolutePath());
    }

    public String getArtist() {
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        if ((artist == null || artist.equals("")) && myMp3Metadata != null) artist = myMp3Metadata.getArtist();

        if (artist == null || artist.equals("")) artist = "Unknown Artist";

        return artist;
    }

    public String getAlbum() {
        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

        if ((album == null || album.equals("")) && myMp3Metadata != null) album = myMp3Metadata.getAlbum();

        if (album == null || album.equals("")) album = "Unknown Album";

        return album;
    }

    public Integer getTrackNumber() {
        String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

        if ((trackNumber == null || trackNumber.equals("")) && myMp3Metadata != null) trackNumber = myMp3Metadata.getAlbum();

        if (trackNumber == null || trackNumber.equals("")) return null;

        try {
            return Integer.parseInt(trackNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getSongTitle() {
        String songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        if ((songName == null || songName.equals("")) && myMp3Metadata != null) songName = myMp3Metadata.getSongTitle();

        return songName;
    }

    public String[] getTags() {
        String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

        if ((genre == null || genre.equals("")) && myMp3Metadata != null) genre = myMp3Metadata.getGenre();

        if (genre == null || genre.equals("")) genre = "Unknown";

        return genre.split(";");
    }

}
