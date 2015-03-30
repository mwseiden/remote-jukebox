package com.theducksparadise.jukebox;

import com.theducksparadise.jukebox.domain.Album;
import com.theducksparadise.jukebox.domain.Artist;
import com.theducksparadise.jukebox.domain.NamedItem;
import com.theducksparadise.jukebox.domain.Song;
import com.theducksparadise.jukebox.util.SystemUiHider;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MusicListActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_music_list);

        ListView listView = (ListView)findViewById(R.id.musicListView);

        listView.setAdapter(new MusicAdapter());

        /*
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        */
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }


    @Override
    public void onBackPressed() {
        ListView listView = (ListView)findViewById(R.id.musicListView);

        if (!((MusicAdapter)listView.getAdapter()).goBack()) {
            super.onBackPressed();
        }
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private class MusicAdapter extends BaseAdapter {
        private NamedItem rootItem = null;
        private Set<NamedItem> selected = new HashSet<NamedItem>();

        @Override
        public int getCount() {
            if (rootItem == null) {
                return MusicDatabase.getInstance(getApplicationContext()).getArtists().size();
            } else if (rootItem instanceof Artist) {
                return ((Artist)rootItem).getAlbums().size();
            } else if (rootItem instanceof Album) {
                return ((Album)rootItem).getSongs().size();
            }

            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (rootItem == null) {
                return MusicDatabase.getInstance(getApplicationContext()).getArtists().get(position);
            } else if (rootItem instanceof Artist) {
                return ((Artist)rootItem).getAlbums().get(position);
            } else if (rootItem instanceof Album) {
                return ((Album)rootItem).getSongs().get(position);
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            if (rootItem == null) {
                return MusicDatabase.getInstance(getApplicationContext()).getArtists().get(position).getId();
            } else if (rootItem instanceof Artist) {
                return ((Artist)rootItem).getAlbums().get(position).getId();
            } else if (rootItem instanceof Album) {
                return ((Album)rootItem).getSongs().get(position).getId();
            }

            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final NamedItem namedItem = (NamedItem)getItem(position);

            View rowView = getLayoutInflater().inflate(R.layout.checkbox_item, parent, false);

            TextView textView = (TextView)rowView.findViewById(R.id.musicText);
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/LCD.otf");
            textView.setTextSize(20.0f);
            textView.setTypeface(typeFace);
            textView.setText(namedItem.getName());

            if (!(namedItem instanceof Song)) textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRootItem(namedItem);
                }
            });

            final CheckBox checkBox = (CheckBox)rowView.findViewById(R.id.musicCheckbox);
            checkBox.setChecked(selected.contains(namedItem));
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        selected.add(namedItem);
                    } else {
                        selected.remove(namedItem);
                    }
                }
            });

            return rowView;
        }

        private void setRootItem(NamedItem rootItem) {
            this.rootItem = rootItem;

            notifyDataSetChanged();
        }

        public boolean goBack() {
            if (rootItem == null) {
                return false;
            } else if (rootItem instanceof Artist) {
                rootItem = null;
            } else if (rootItem instanceof Album) {
                rootItem = ((Album)rootItem).getArtist();
            }

            notifyDataSetChanged();

            return true;
        }
    }
}
