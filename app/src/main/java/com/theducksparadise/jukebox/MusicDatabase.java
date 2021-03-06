package com.theducksparadise.jukebox;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.theducksparadise.jukebox.domain.Album;
import com.theducksparadise.jukebox.domain.Artist;
import com.theducksparadise.jukebox.domain.NamedItem;
import com.theducksparadise.jukebox.domain.Song;
import com.theducksparadise.jukebox.metadata.MetadataRetriever;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MusicDatabase extends SQLiteOpenHelper {

    //private static final int DATABASE_VERSION_001 = 2;
    private static final int DATABASE_VERSION_002 = 3;
    private static final int DATABASE_VERSION_003 = 4;
    private static final int DATABASE_VERSION = DATABASE_VERSION_003;

    private static final String DATABASE_NAME = "jukebox_music";

    private static final String ARTIST_TABLE_NAME = "artist";
    private static final String ALBUM_TABLE_NAME = "album";
    private static final String SONG_TABLE_NAME = "song";
    private static final String QUEUE_TABLE_NAME = "queue";
    private static final String TAG_TABLE_NAME = "tag";
    private static final String TAG_SONG_TABLE_NAME = "tag_song";

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

    private static final String QUEUE_TABLE_CREATE =
            "CREATE TABLE " + QUEUE_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "song_id INTEGER, " +
                    "sequence INTEGER, " +
                    "FOREIGN KEY(song_id) REFERENCES " + SONG_TABLE_NAME + "(id));";

    private static final String TAG_TABLE_CREATE =
            "CREATE TABLE " + TAG_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT);";

    private static final String TAG_SONG_TABLE_CREATE =
            "CREATE TABLE " + TAG_SONG_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "tag_id INTEGER, " +
                    "song_id INTEGER, " +
                    "FOREIGN KEY(tag_id) REFERENCES " + TAG_TABLE_NAME + "(id), " +
                    "FOREIGN KEY(song_id) REFERENCES " + SONG_TABLE_NAME + "(id));";

    private List<Artist> artists;
    private List<Artist> filteredArtists;

    private Map<String, Artist> artistIndex;
    private Map<String, Artist> filteredArtistIndex;
    private Map<Integer, Song> songIndex;
    private Map<Integer, Song> filteredSongIndex;

    private LinkedHashMap<String, List<Song>> tags;

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
        db.execSQL(QUEUE_TABLE_CREATE);
        db.execSQL(TAG_TABLE_CREATE);
        db.execSQL(TAG_SONG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION_002) {
            db.execSQL(QUEUE_TABLE_CREATE);
        } else if (oldVersion < DATABASE_VERSION_003) {
            db.execSQL(TAG_TABLE_CREATE);
            db.execSQL(TAG_SONG_TABLE_CREATE);
        }
    }

    public List<Artist> getArtists() {
        return filteredArtists == null ? artists : filteredArtists;
    }

    public List<Artist> getArtists(boolean filtered) {
        return (filteredArtists == null || !filtered) ? artists : filteredArtists;
    }

    public Artist getArtist(String name) {
        return filteredArtistIndex == null ? artistIndex.get(name) : filteredArtistIndex.get(name);
    }

    public Artist getArtistCaseInsensitive(String name, boolean filter) {
        return searchArtistInsensitive(name, (filteredArtistIndex == null || !filter) ? artistIndex : filteredArtistIndex);
    }

    private Artist searchArtistInsensitive(String name, Map<String, Artist> list) {
        for (Map.Entry<String, Artist> entry : list.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) return entry.getValue();
        }

        return null;
    }

    public LinkedHashMap<String, List<Song>> getTags() { return tags; }

    public void synchronizeWithFileSystem(String path, AsyncProgress asyncProgress) {
        clearDatabase(asyncProgress);
        rebuildDatabase(path, asyncProgress);
        sort(asyncProgress);
    }

    public void clearDatabase() {
        clearDatabase(null);
    }

    public void saveQueue(final Song currentSong, final Collection<Song> queue) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getWritableDatabase().beginTransaction();

                deleteSavedQueue();

                if (currentSong != null) saveQueueItem(currentSong, 0);

                int i = 1;
                for (Song song: queue) {
                    saveQueueItem(song, i);
                    i++;
                }

                getWritableDatabase().setTransactionSuccessful();
                getWritableDatabase().endTransaction();

            }
        }).start();
    }

    public List<Song> loadQueue() {
        List<Song> queue = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT song_id FROM " + QUEUE_TABLE_NAME + " ORDER BY sequence",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Song song = getSongIndex(false).get(cursor.getInt(0));

                if (song != null) queue.add(song);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return queue;
    }

    public void deleteSavedQueue() {
        getWritableDatabase().execSQL("DELETE FROM " + QUEUE_TABLE_NAME + ";");
    }

    public Song getRandomSong() {
        Random random = new Random();
        Object[] songs = getSongIndex().values().toArray();
        return songs.length > 0 ? (Song)songs[random.nextInt(songs.length)] : null;
    }

    public List<Song> findSong(String name, boolean filter) {
        List<Song> songs = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM " + SONG_TABLE_NAME + " WHERE LOWER(name) = ?",
                new String[] { name.toLowerCase() }
        );

        if (cursor.moveToFirst()) {
            do {
                Song song = getSongIndex(filter).get(cursor.getInt(0));

                if (song != null) songs.add(song);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return songs;
    }

    public void filter(String tagList, String exclusionList) {
        if ((tagList == null || tagList.equals("")) && (exclusionList == null || exclusionList.equals(""))) {
            filteredArtists = null;
            filteredArtistIndex = null;
            filteredSongIndex = null;
        } else {
            Set<Song> filteredSongs = new HashSet<>();

            if (tagList == null || tagList.equals("")) {
                filteredSongs.addAll(songIndex.values());
            } else {
                String[] filtered = tagList.split(";");

                for (String tag : filtered) {
                    List<Song> songs = tags.get(tag);

                    if (songs != null) filteredSongs.addAll(songs);
                }
            }

            if (exclusionList != null && !exclusionList.equals("")) {
                String[] exclusions = exclusionList.split(";");

                for (String tag : exclusions) {
                    List<Song> songs = tags.get(tag);

                    if (songs != null) filteredSongs.removeAll(songs);
                }
            }

            filteredArtists = new ArrayList<>();
            filteredArtistIndex = new HashMap<>();
            filteredSongIndex = new HashMap<>();

            for (Song song : filteredSongs) {
                Album songAlbum = song.getAlbum();
                Artist songArtist = songAlbum.getArtist();
                Artist artist = getArtist(songArtist.getName());

                if (artist == null) {
                    artist = new Artist();
                    artist.setId(songArtist.getId());
                    artist.setName(songArtist.getName());

                    filteredArtists.add(artist);
                    filteredArtistIndex.put(artist.getName(), artist);
                }

                Album album = artist.getAlbum(song.getAlbum().getName());

                if (album == null) {
                    album = new Album();

                    album.setId(songAlbum.getId());
                    album.setName(songAlbum.getName());
                    album.setArtist(artist);

                    artist.getAlbums().add(album);
                }

                Song filteredSong = new Song();
                filteredSong.setId(song.getId());
                filteredSong.setName(song.getName());
                filteredSong.setSequence(song.getSequence());
                filteredSong.setFileName(song.getFileName());
                filteredSong.setAlbum(album);

                album.getSongs().add(filteredSong);
                filteredSongIndex.put(filteredSong.getId(), filteredSong);
            }

            sort(null);
        }
    }

    private Map<Integer, Song> getSongIndex() {
        return getSongIndex(true);
    }

    private Map<Integer, Song> getSongIndex(boolean filter) {
        return (filteredSongIndex == null || !filter) ? songIndex : filteredSongIndex;
    }

    private void reloadDatabase() {
        artists = new ArrayList<>();
        artistIndex = new HashMap<>();
        songIndex = new HashMap<>();
        tags = new LinkedHashMap<>();
        filteredArtists = null;
        filteredArtistIndex = null;
        filteredSongIndex = null;

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

        loadTags();
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
                songIndex.put(song.getId(), song);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private void loadTags() {
        Map<String, Integer> tagIds = new LinkedHashMap<>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, name FROM " + TAG_TABLE_NAME + " ORDER BY name",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                tagIds.put(cursor.getString(1), cursor.getInt(0));
                tags.put(cursor.getString(1), new ArrayList<Song>());
            } while (cursor.moveToNext());
        }

        cursor.close();

        loadTagSongs(tagIds);
    }

    private void loadTagSongs(Map<String, Integer> tagIds) {
        for (Map.Entry<String, Integer> entry : tagIds.entrySet()) {
            Cursor cursor = getReadableDatabase().rawQuery(
                    "SELECT song_id FROM " + TAG_SONG_TABLE_NAME + " WHERE tag_id = ?",
                    new String[]{entry.getValue().toString()}
            );

            if (cursor.moveToFirst()) {
                do {
                    tags.get(entry.getKey()).add(songIndex.get(cursor.getInt(0)));
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

    }

    // TODO: Refactor this so it isn't so redundant
    @SuppressWarnings("unused")
    private void removeMissingFiles() {
        List<Artist> emptyArtists = new ArrayList<>();

        for (Artist artist: artists) {
            List<Album> emptyAlbums = new ArrayList<>();

            for (Album album: artist.getAlbums()) {
                List<Song> removedSongList = new ArrayList<>();

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
            artistIndex.remove(artist.getName());
        }
    }

    private List<File> findAllSongFiles(String path, AsyncProgress asyncProgress) {
        File root = new File(path);

        if (!root.exists()) return null;

        List<File> files = new ArrayList<>();

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
        if (asyncProgress != null) asyncProgress.updateProgress("Clearing Queue");
        deleteSavedQueue();

        if (asyncProgress != null) asyncProgress.updateProgress("Clearing Songs");
        getWritableDatabase().execSQL("DELETE FROM " + SONG_TABLE_NAME + ";");

        if (asyncProgress != null) asyncProgress.updateProgress("Clearing Albums");
        getWritableDatabase().execSQL("DELETE FROM " + ALBUM_TABLE_NAME + ";");

        if (asyncProgress != null) asyncProgress.updateProgress("Clearing Artists");
        getWritableDatabase().execSQL("DELETE FROM " + ARTIST_TABLE_NAME + ";");

        if (asyncProgress != null) asyncProgress.updateProgress("Clearing Song Tags");
        getWritableDatabase().execSQL("DELETE FROM " + TAG_SONG_TABLE_NAME + ";");

        if (asyncProgress != null) asyncProgress.updateProgress("Clearing Tags");
        getWritableDatabase().execSQL("DELETE FROM " + TAG_TABLE_NAME + ";");

        artists.clear();
        artistIndex.clear();
        songIndex.clear();
        tags.clear();
        filteredArtists = null;
        filteredArtistIndex = null;
        filteredSongIndex = null;
    }

    private void rebuildDatabase(String path, AsyncProgress asyncProgress) {
        List<File> files = findAllSongFiles(path, asyncProgress);

        int i = 0;
        for (File file : files) {
            try {
                i++;
                asyncProgress.updateProgress("Processing File " + i + " Of " + files.size());

                MetadataRetriever metadata = new MetadataRetriever(file);

                String artistName = metadata.getArtist();
                String albumName = metadata.getAlbum();
                String songName = metadata.getSongTitle();

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

                int sequence = metadata.getTrackNumber() == null ? album.getSongs().size() + 1 : metadata.getTrackNumber();
                if (songName == null || songName.equals("")) songName = "Track " + sequence;

                Song song = new Song();
                song.setName(songName);
                song.setFileName(file.getAbsolutePath());
                song.setSequence(sequence);
                song.setAlbum(album);
                album.getSongs().add(song);
                songIndex.put(song.getId(), song);

                String[] songTags = metadata.getTags();

                for (String tag : songTags) {
                    List<Song> taggedSongs = tags.get(tag);

                    if (taggedSongs == null) {
                        taggedSongs = new ArrayList<>();
                        tags.put(tag, taggedSongs);
                    }

                    taggedSongs.add(song);
                }

            } catch (ArrayIndexOutOfBoundsException e) {
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

        for (Map.Entry<String, List<Song>> entry : tags.entrySet()) {
            asyncProgress.updateProgress("Saving Tag " + entry.getKey());

            Integer tagId = saveTag(entry.getKey());

            for (Song song : entry.getValue()) {
                saveTagSong(tagId, song);
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

    private void saveQueueItem(Song song, Integer sequence) {
        getWritableDatabase().execSQL(
                "INSERT INTO " + QUEUE_TABLE_NAME + " (song_id, sequence) VALUES (?, ?);",
                new Object[]{ song.getId(), sequence }
        );
    }

    private Integer saveTag(String name) {
        getWritableDatabase().execSQL(
                "INSERT INTO " + TAG_TABLE_NAME + " (name) VALUES (?);",
                new Object[]{ name }
        );

        Integer result = null;

        Cursor cursor = getWritableDatabase().rawQuery("SELECT last_insert_rowid() FROM " + TAG_TABLE_NAME + ";", null);

        if (cursor.moveToFirst()) result = cursor.getInt(0);

        cursor.close();

        return result;
    }

    private void saveTagSong(Integer tagId, Song song) {
        getWritableDatabase().execSQL(
                "INSERT INTO " + TAG_SONG_TABLE_NAME + " (tag_id, song_id) VALUES (?, ?);",
                new Object[]{tagId, song.getId()}
        );
    }

    private void sort(AsyncProgress asyncProgress) {
        if (asyncProgress != null) asyncProgress.updateProgress("Sorting");
        sortNamedItems(getArtists());
        for (Artist artist: getArtists()) {
            sortNamedItems(artist.getAlbums());
            for (Album album: artist.getAlbums()) {
                sortSongs(album.getSongs());
            }
        }
    }

    private <T extends NamedItem> void sortNamedItems(List<T> items) {
        Collections.sort(items, new Comparator<NamedItem>() {
            @Override
            public int compare(NamedItem lhs, NamedItem rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    private void sortSongs(List<Song> songs) {
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return Integer.compare(lhs.getSequence(), rhs.getSequence());
            }
        });
    }
}
