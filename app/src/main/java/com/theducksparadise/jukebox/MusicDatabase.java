package com.theducksparadise.jukebox;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.theducksparadise.jukebox.domain.Album;
import com.theducksparadise.jukebox.domain.Artist;
import com.theducksparadise.jukebox.domain.Song;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, Artist> artistIndex;

    private static volatile MusicDatabase instance;

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

    public Artist getArtist(String name) {
        return artistIndex.get(name);
    }

    public void synchronizeWithFileSystem(String path, AsyncProgress asyncProgress) {
        clearDatabase(asyncProgress);
        rebuildDatabase(path, asyncProgress);
    }

    private void reloadDatabase() {
        artists = new ArrayList<Artist>();
        artistIndex = new HashMap<String, Artist>();

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
                artistIndex.put(artist.getName(), artist);
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

                album.getSongsForQueue().add(song);
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
                    album.getSongsForQueue().remove(song);
                }

                if (album.getSongsForQueue().size() == 0) emptyAlbums.add(album);
            }

            for (Album album: emptyAlbums) {
                artist.getAlbums().remove(album);
            }

            if (artist.getAlbums().size() == 0) emptyArtists.add(artist);
        }

        for (Artist artist: emptyArtists) {
            artists.remove(artist);
            artistIndex.remove(artist.getName());
        }
    }

    private List<File> findAllSongFiles(String path, AsyncProgress asyncProgress) {
        File root = new File(path);

        if (!root.exists()) return null;

        List<File> files = new ArrayList<File>();

        addFilesToList(root, files, asyncProgress);

        return files;
    }

    private void addFilesToList(File root, List<File> files, AsyncProgress asyncProgress) {
        asyncProgress.updateProgress("Searching " + root.getAbsolutePath());

        files.addAll(Arrays.asList(root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory() && pathname.getName().toLowerCase().endsWith(".mp3");
            }
        })));

        File[] subdirectories = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        for (File subdirectory: subdirectories) {
            addFilesToList(subdirectory, files, asyncProgress);
        }
    }

    private void clearDatabase(AsyncProgress asyncProgress) {
        asyncProgress.updateProgress("Clearing Songs");
        getWritableDatabase().execSQL("DELETE FROM " + SONG_TABLE_NAME + ";");

        asyncProgress.updateProgress("Clearing Albums");
        getWritableDatabase().execSQL("DELETE FROM " + ALBUM_TABLE_NAME + ";");

        asyncProgress.updateProgress("Clearing Artists");
        getWritableDatabase().execSQL("DELETE FROM " + ARTIST_TABLE_NAME + ";");

        artists.clear();
        artistIndex.clear();
    }

    private void rebuildDatabase(String path, AsyncProgress asyncProgress) {
        List<File> files = findAllSongFiles(path, asyncProgress);

        int i = 0;
        for (File file : files) {
            try {
                i++;
                asyncProgress.updateProgress("Processing File " + i + " Of " + files.size());
                MusicMetadataSet metadataSet = new MyID3().read(file);
                IMusicMetadata metadata = metadataSet.getSimplified();
                String artistName = metadata.getArtist();
                String albumName = metadata.getAlbum();
                String songName = metadata.getSongTitle();

                if (artistName == null || artistName.equals("")) artistName = "Unknown Artist";
                if (albumName == null || albumName.equals("")) albumName = "Unknown Album";

                Artist artist = getArtist(artistName);

                if (artist == null) {
                    artist = new Artist();
                    artist.setName(artistName);
                    artists.add(artist);
                    artistIndex.put(artistName, artist);
                }

                Album album = artist.getAlbum(albumName);

                if (album == null) {
                    album = new Album();
                    album.setName(albumName);
                    album.setArtist(artist);
                    artist.getAlbums().add(album);
                }

                int sequence = metadata.getTrackNumber() == null ? album.getSongsForQueue().size() + 1 : metadata.getTrackNumber().intValue();
                if (songName == null || songName.equals("")) songName = "Track " + sequence;

                Song song = new Song();
                song.setName(songName);
                song.setFileName(file.getAbsolutePath());
                song.setSequence(sequence);
                song.setAlbum(album);
                album.getSongsForQueue().add(song);

            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                // Well that sucks. Ignore it.
            }
        }

        i = 0;
        for (Artist artist : artists) {
            i++;

            asyncProgress.updateProgress("Saving Artist " + i + " Of " + artists.size());

            saveArtist(artist);

            for (Album album : artist.getAlbums()) {
                saveAlbum(album);

                for (Song song : album.getSongs()) {
                    saveSong(song);
                }
            }
        }
    }

    private void saveArtist(Artist artist) {
        getWritableDatabase().execSQL(
                "INSERT INTO " + ARTIST_TABLE_NAME + " (name) VALUES (?);",
                new Object[] { artist.getName() }
        );

        Cursor cursor = getWritableDatabase().rawQuery("SELECT last_insert_rowid() FROM " + ARTIST_TABLE_NAME, null);

        if (cursor.moveToFirst()) artist.setId(cursor.getInt(0));

        cursor.close();
    }

    private void saveAlbum(Album album) {
        getWritableDatabase().execSQL(
                "INSERT INTO " + ALBUM_TABLE_NAME + " (name, artist_id) VALUES (?, ?);",
                new Object[] { album.getName(), album.getArtist().getId() }
        );

        Cursor cursor = getWritableDatabase().rawQuery("SELECT last_insert_rowid() FROM " + ALBUM_TABLE_NAME + ";", null);

        if (cursor.moveToFirst()) album.setId(cursor.getInt(0));

        cursor.close();
    }

    private void saveSong(Song song) {
        getWritableDatabase().execSQL(
                "INSERT INTO " + SONG_TABLE_NAME + " (name, album_id, file_name, sequence) VALUES (?, ?, ?, ?);",
                new Object[] { song.getName(), song.getAlbum().getId(), song.getFileName(), song.getSequence() }
        );

        Cursor cursor = getWritableDatabase().rawQuery("SELECT last_insert_rowid() FROM " + SONG_TABLE_NAME + ";", null);

        if (cursor.moveToFirst()) song.setId(cursor.getInt(0));

        cursor.close();
    }
}
