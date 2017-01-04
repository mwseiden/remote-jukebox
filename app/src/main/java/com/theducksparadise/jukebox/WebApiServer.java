package com.theducksparadise.jukebox;

import android.content.res.AssetManager;

import com.theducksparadise.jukebox.domain.Song;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WebApiServer extends NanoHTTPD {

    private Map<String, byte[]> pages;

    public WebApiServer(AssetManager assetManager) throws IOException {
        super(8080);
        loadPages(assetManager);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String path = session.getUri().toLowerCase();
        switch (path) {
            case "/api/v1/status":
                return statusResponse();
            default:
                byte[] data = pages.get(path);

                if (data == null || data.length == 0) {
                    return newFixedLengthResponse(
                            Response.Status.NOT_FOUND,
                            NanoHTTPD.MIME_PLAINTEXT,
                            "Unknown"
                    );
                }

                return newFixedLengthResponse(
                        Response.Status.OK,
                        getMimeTypeForFile(path),
                        new ByteArrayInputStream(data),
                        data.length
                );
        }
    }

    private Response statusResponse() {
        Song song = JukeboxMedia.getInstance().getCurrentSong();
        JSONObject json = new JSONObject();

        if (song != null) {
            try {
                json.put("id", song.getId());
                json.put("name", song.getName());
                json.put("artist", song.getAlbum().getArtist().getName());
                json.put("album", song.getAlbum().getName());
            } catch (JSONException e) {
                return newServerError(e);
            }
        }

        return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                json.toString()
        );
    }

    private Response newServerError(Exception e) {
        return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                NanoHTTPD.MIME_PLAINTEXT,
                "Internal Error: " + e.getMessage()
        );
    }

    private void loadPages(AssetManager assetManager) throws IOException {
        pages = new HashMap<>();

        String[] assets = assetManager.list("http");

        for (String asset : assets) {
            InputStream inputStream = null;
            try {
                inputStream = assetManager.open("http/" + asset);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                buffer.flush();

                pages.put("/" + asset.toLowerCase(), buffer.toByteArray());

            } catch (IOException e) {
                if (inputStream != null) inputStream.close();
            }
        }
    }
}
