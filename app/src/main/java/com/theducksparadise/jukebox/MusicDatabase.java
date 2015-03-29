package com.theducksparadise.jukebox;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.theducksparadise.jukebox.domain.Album;
import com.theducksparadise.jukebox.domain.Artist;
import com.theducksparadise.jukebox.domain.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "jukebox_music";

    private static final String ARTIST_TABLE_NAME = "artist";
    private static final String ALBUM_TABLE_NAME = "album";
    private static final String SONG_TABLE_NAME = "song";

    private static final String ARTIST_TABLE_CREATE =
            "CREATE TABLE " + ARTIST_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT);";

    private static final String ALBUM_TABLE_CREATE =
            "CREATE TABLE " + ALBUM_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "artist_id INTEGER, " +
                    "name TEXT, " +
                    "FOREIGN KEY(artist_id) REFERENCES " + ARTIST_TABLE_NAME + "(id));";

    private static final String SONG_TABLE_CREATE =
            "CREATE TABLE " + SONG_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "album_id INTEGER, " +
                    "name TEXT, " +
                    "file_name TEXT, " +
                    "sequence INTEGER, " +
                    "FOREIGN KEY(album_id) REFERENCES " + ALBUM_TABLE_NAME + "(id));";

    private List<Artist> artists;

    private static MusicDatabase instance;

    public static MusicDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (MusicDatabase.class) {
                if (instance == null) {
                    instance = new MusicDatabase(context);
                }
            }
        }

        return instance;
    }

    private MusicDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        reloadDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ARTIST_TABLE_CREATE);
        db.execSQL(ALBUM_TABLE_CREATE);
        db.execSQL(SONG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // dunno
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void synchronizeWithFileSystem(String path) {
        removeMissingFiles();
        findAllSongFiles(path);
    }

    private void reloadDatabase() {
        artists = new ArrayList<Artist>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, name FROM " + ARTIST_TABLE_NAME + " ORDER BY name",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Artist artist = new Artist();

                artist.setId(cursor.getInt(0));
                artist.setName(cursor.getString(1));

                artists.add(artist);
            } while (cursor.moveToNext());
        }

        cursor.close();

        for (Artist artist: artists) {
            loadAlbums(artist);
        }
    }

    private void loadAlbums(Artist artist) {
        artist.setAlbums(new ArrayList<Album>());

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, name FROM " + ALBUM_TABLE_NAME +
                        " WHERE artist_id = " + artist.getId() + " ORDER BY name",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Album album = new Album();

                album.setId(cursor.getInt(0));
                album.setName(cursor.getString(1));
                album.setArtist(artist);

                artist.getAlbums().add(album);
            } while (cursor.moveToNext());
        }

        cursor.close();

        for (Album album: artist.getAlbums()) {
            loadSongs(album);
        }
    }

    private void loadSongs(Album album) {
        album.setSongs(new ArrayList<Song>());

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, name, sequence, file_name FROM " + SONG_TABLE_NAME +
                        " WHERE album_id = " + album.getId() + " ORDER BY sequence",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();

                song.setId(cursor.getInt(0));
                song.setName(cursor.getString(1));
                song.setSequence(cursor.getInt(2));
                song.setFileName(cursor.getString(3));
                song.setAlbum(album);

                album.getSongs().add(song);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    // TODO: Refactor this so it isn't so redundant
    private void removeMissingFiles() {
        List<Artist> emptyArtists = new ArrayList<Artist>();

        for (Artist artist: artists) {
            List<Album> emptyAlbums = new ArrayList<Album>();

            for (Album album: artist.getAlbums()) {
                List<Song> removedSongList = new ArrayList<Song>();

                for (Song song: album.getSongs()) {
                    if (!new File(song.getFileName()).exists()) {
                        removedSongList.add(song);
                    }
                }

                for (Song song: removedSongList) {
                    album.getSongs().remove(song);
                }

                if (album.getSongs().size() == 0) emptyAlbums.add(album);
            }

            for (Album album: emptyAlbums) {
                artist.getAlbums().remove(album);
            }

            if (artist.getAlbums().size() == 0) emptyArtists.add(artist);
        }

        for (Artist artist: emptyArtists) {
            artists.remove(artist);
        }
    }

    private List<File> findAllSongFiles(String path) {
        return null;
    }
}
