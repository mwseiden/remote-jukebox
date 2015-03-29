package com.theducksparadise.jukebox;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public abstract class WaitActivityAsyncTask extends AsyncTask<Void, Integer, Boolean> {

    private RefreshDatabaseWaitActivity activity;
    private Handler handler;

    public void setActivity(RefreshDatabaseWaitActivity activity) {
        this.activity = activity;
    }

    public RefreshDatabaseWaitActivity getActivity() {
        return activity;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    protected void setProgressMessage(String text) {
        Message message = new Message();

        message.what = RefreshDatabaseWaitActivity.UPDATE_MESSAGE;
        message.obj = text;

        handler.sendMessage(message);
    }
}
