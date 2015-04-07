package com.theducksparadise.jukebox;

import com.theducksparadise.jukebox.domain.Album;
import com.theducksparadise.jukebox.domain.Artist;
import com.theducksparadise.jukebox.domain.NamedItem;
import com.theducksparadise.jukebox.domain.Song;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;


public class MusicListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_music_list);

        ListView listView = (ListView)findViewById(R.id.musicListView);

        final MusicAdapter musicAdapter = new MusicAdapter();

        listView.setAdapter(musicAdapter);

        ImageButton queueButton = (ImageButton)findViewById(R.id.queueButton);

        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JukeboxMedia.getInstance().addToQueue(musicAdapter.getSelected());
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ListView listView = (ListView)findViewById(R.id.musicListView);

        if (!((MusicAdapter)listView.getAdapter()).goBack()) {
            super.onBackPressed();
        }
    }

    private class MusicAdapter extends BaseAdapter {
        private NamedItem rootItem = null;
        private Set<NamedItem> selected = new LinkedHashSet<>();
        private Stack<Integer> positions = new Stack<>();

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
        public View getView(final int position, View convertView, ViewGroup parent) {
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
                    positions.push(position);
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

                    updateQueueButton();
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

            ListView listView = (ListView)findViewById(R.id.musicListView);
            listView.setSelection(positions.pop());

            return true;
        }

        public Set<NamedItem> getSelected() {
            return selected;
        }

        private void updateQueueButton() {
            ImageButton queueButton = (ImageButton)findViewById(R.id.queueButton);

            if (selected.isEmpty()) {
                queueButton.setBackgroundResource(R.drawable.queue_unavailable);
            } else {
                queueButton.setBackgroundResource(R.drawable.queue);
            }
        }
    }
}
