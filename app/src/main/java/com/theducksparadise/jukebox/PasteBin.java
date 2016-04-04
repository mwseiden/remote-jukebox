package com.theducksparadise.jukebox;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PasteBin {

    private URL pasteBinUrl;

    public PasteBin() {
        try {
            pasteBinUrl = new URL("http://pastebin.com/api/api_post.php");
        } catch (MalformedURLException e) {
            pasteBinUrl = null; // never going to happen
        }
    }

    public String post(String apiKey, String userKey, String text) {
        URL pasteBinUrl;
        HttpURLConnection connection = null;

        try {
            pasteBinUrl = new URL("http://pastebin.com/api/api_post.php");
            connection = (HttpURLConnection)pasteBinUrl.openConnection();
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            // &api_paste_expire_date='.$api_paste_expire_date.'
            // &api_paste_format='.$api_paste_format.'
            String requestOptions = "api_option=paste&api_user_key=" + userKey + "&api_paste_private=1&api_paste_name=Bands You Can Request&api_dev_key=" + apiKey + "&api_paste_code=" + text;

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(requestOptions.getBytes());
            out.close();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            String contentUrl = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
            in.close();

            return contentUrl;
        } catch (IOException e) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
