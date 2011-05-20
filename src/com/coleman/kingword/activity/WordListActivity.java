
package com.coleman.kingword.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.wordlist.WordListManager;
import com.coleman.kingword.wordlist.WordList.InternalWordList;
import com.coleman.kingword.wordlist.WordListManager.LoadNotifier;

public class WordListActivity extends Activity implements OnItemClickListener, OnClickListener {
    private WordListAdapter adapter;

    private Cursor c;

    private static final String projection[] = new String[] {
            WordsList._ID, WordsList.DESCRIBE, WordsList.PATH_NAME, WordsList.SET_METHOD
    };

    private ListView listView;

    private Button loadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list);
        listView = (ListView) findViewById(R.id.listView1);
        loadBtn = (Button) findViewById(R.id.button1);
        c = getContentResolver().query(WordsList.CONTENT_URI, projection, null, null, null);
        if (!c.moveToFirst()) {
            LoadNotifier notifier = new LoadNotifier() {
                @Override
                public void notifyProgress(int p) {
                }

                @Override
                public void notifyDone() {
                    c.requery();
                    adapter.notifyDataSetChanged();
                }
            };
            WordListManager.getInstance().loadWordListFromFile(this,
                    InternalWordList.POSTGRADUATE_WORDLIST, true, notifier);
        }
        adapter = new WordListAdapter(this, R.layout.word_list_item, c);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        loadBtn.setOnClickListener(this);
        listView.setEmptyView(loadBtn);
    }

    private class WordListAdapter extends ResourceCursorAdapter {
        public WordListAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            view.setTag(cursor.getLong(0));
            TextView tv = (TextView) view.findViewById(R.id.textView1);
            String text = cursor.getLong(0) + ":" + cursor.getString(2);
            tv.setText(text);
            tv.setLines(3);
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(WordListActivity.this, SubWordListActivity.class);
                    i.putExtra(WordsList._ID, cursor.getLong(0));
                    startActivity(i);
                }
            });
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return super.newView(context, cursor, parent);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                startActivity(new Intent(this, FileExplorer.class));
                break;
            default:
                break;
        }
    }
}
