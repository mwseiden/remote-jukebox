package com.theducksparadise.jukebox;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class WebApiService extends Service {
    private WebApiServer webApiServer;

    public WebApiService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        webApiServer = null;

        try {
            if (getApplicationContext().getSharedPreferences(SettingsActivity.PREFERENCE_FILE, Context.MODE_PRIVATE).getBoolean(SettingsActivity.PREFERENCE_KEY_WEB_SERVER_ENABLED, false)) {
                webApiServer = new WebApiServer(getAssets());
                Log.i("WebApiServer", "Started Web Service Listening");
            } else {
                Log.i("WebApiServer", "Started Web Service Ignoring");
            }
        } catch (IOException e) {
            webApiServer = null;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        if (webApiServer != null) {
            webApiServer.stop();
        }

        webApiServer = null;

        Log.i("WebApiServer", "Stopped Web Service");
    }
}
