package com.theducksparadise.jukebox;

import android.os.AsyncTask;

public abstract class WaitActivityAsyncTask extends AsyncTask<Void, Integer, Boolean> {

    private RefreshDatabaseWaitActivity activity;

    public void setActivity(RefreshDatabaseWaitActivity activity) {
        this.activity = activity;
    }

    public RefreshDatabaseWaitActivity getActivity() {
        return activity;
    }
}
