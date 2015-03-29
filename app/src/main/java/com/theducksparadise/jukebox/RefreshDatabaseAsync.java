package com.theducksparadise.jukebox;

import android.content.Context;

public class RefreshDatabaseAsync extends WaitActivityAsyncTask {
    private String path;
    private Context context;

    @Override
    protected Boolean doInBackground(Void... params) {
        if (path == null || context == null || getActivity() == null) return false;

        MusicDatabase.getInstance(context).synchronizeWithFileSystem(path, new AsyncProgress() {
            @Override
            public void updateProgress(String text) {
                setProgressMessage(text);
            }
        });

        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        getActivity().finish();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
