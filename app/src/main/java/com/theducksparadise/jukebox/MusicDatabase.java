package com.theducksparadise.jukebox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
                    "sequence INTEGER, " +
                    "FOREIGN KEY(album_id) REFERENCES " + ALBUM_TABLE_NAME + "(id));";

    MusicDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
}
