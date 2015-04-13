package com.theducksparadise.jukebox;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.theducksparadise.jukebox.domain.Song;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TagListActivity extends Activity {
    public static final String CHOSEN_TAGS = "chosenTags";
    public static final String SELECTED_TAGS = "selectedTags";
    public static final String WINDOW_TYPE = "windowType";
    public static final int PICK_TAGS = 43526662;

    private int windowType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_music_list);

        ListView listView = (ListView)findViewById(R.id.musicListView);

        final TagAdapter tagAdapter = new TagAdapter();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tagAdapter.setSelected(extras.getString(SELECTED_TAGS));
            windowType = extras.getInt(WINDOW_TYPE);
        }

        listView.setAdapter(tagAdapter);

        ImageButton queueButton = (ImageButton)findViewById(R.id.queueButton);

        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnTags(tagAdapter.getSelected());
                finish();
            }
        });
    }

    private void returnTags(String tags) {
        Intent result = new Intent();
        result.putExtra(CHOSEN_TAGS, tags);
        result.putExtra(WINDOW_TYPE, windowType);
        setResult(RESULT_OK, result);
        finish();
    }

    private class TagAdapter extends BaseAdapter {
        private Set<String> selected = new LinkedHashSet<>();

        @Override
        public int getCount() {
            return MusicDatabase.getInstance(getApplicationContext()).getTags().size();
        }

        @Override
        public Object getItem(int position) {
            LinkedHashMap<String, List<Song>> tags = MusicDatabase.getInstance(getApplicationContext()).getTags();

            Object[] tagNames = tags.keySet().toArray();

            if (position >= 0 && position < tagNames.length) return tagNames[position].toString();

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final String tagName = (String)getItem(position);

            View rowView = getLayoutInflater().inflate(R.layout.checkbox_item, parent, false);

            TextView textView = (TextView)rowView.findViewById(R.id.musicText);
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/LCD.otf");
            textView.setTextSize(20.0f);
            textView.setTypeface(typeFace);
            textView.setText(tagName);

            final CheckBox checkBox = (CheckBox)rowView.findViewById(R.id.musicCheckbox);
            checkBox.setChecked(selected.contains(tagName));
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        selected.add(tagName);
                    } else {
                        selected.remove(tagName);
                    }

                    //updateQueueButton();
                }
            });

            return rowView;
        }

        public String getSelected() {
            StringBuilder stringBuilder = new StringBuilder();

            boolean notFirst = false;
            for (String selection : selected) {
                if (notFirst) stringBuilder.append(";");

                stringBuilder.append(selection);

                notFirst = true;
            }

            return stringBuilder.toString();
        }

        public void setSelected(String selection) {
            String[] tags = selection.split(";");

            selected.clear();

            for (String tag : tags) {
                selected.add(tag);
            }
        }

        /*
        private void updateQueueButton() {
            ImageButton queueButton = (ImageButton)findViewById(R.id.queueButton);

            if (selected.isEmpty()) {
                queueButton.setBackgroundResource(R.drawable.queue_unavailable);
            } else {
                queueButton.setBackgroundResource(R.drawable.queue);
            }
        }
        */
    }
}
