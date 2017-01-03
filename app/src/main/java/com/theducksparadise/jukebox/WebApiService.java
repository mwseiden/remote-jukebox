package com.theducksparadise.jukebox;

import android.app.Service;
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
        try {
            webApiServer = new WebApiServer(getAssets());
        } catch (IOException e) {
            webApiServer = null;
        }

        Log.i("WebApiServer", "Started Web Service");

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
