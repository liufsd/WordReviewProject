
package com.coleman.kingword.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.wordlist.WordListManager;
import com.coleman.kingword.wordlist.WordList.InternalWordList;
import com.coleman.kingword.wordlist.WordListManager.IProgressNotifier;

public class WordListActivity extends Activity implements OnItemClickListener, OnClickListener,
        OnMenuItemClickListener {
    private static final String TAG = WordListActivity.class.getName();
    private WordListAdapter adapter;

    private Cursor c;

    private static final String projection[] = new String[] {
            WordsList._ID, WordsList.DESCRIBE, WordsList.PATH_NAME, WordsList.SET_METHOD
    };

    private ListView listView;

    private Button loadBtn;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list);
        listView = (ListView) findViewById(R.id.listView1);
        loadBtn = (Button) findViewById(R.id.button1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        listView.setOnItemClickListener(this);
        loadBtn.setOnClickListener(this);
        new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.wordlist_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_load:
                startActivity(new Intent(this, FileExplorer.class));
                break;
        }
        return true;
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private class ExpensiveTask extends AsyncTask<Void, Integer, Void> {
        private byte type;

        private static final byte INIT_QUERY = 0;

        public ExpensiveTask(byte initQuery) {
            this.type = initQuery;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case INIT_QUERY:
                    progressBar.setVisibility(View.VISIBLE);
                    findViewById(R.id.loadLayout).setVisibility(View.GONE);
                    findViewById(R.id.view1).setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (type) {
                case INIT_QUERY:
                    c = getContentResolver().query(WordsList.CONTENT_URI, projection, null, null,
                            null);
                    if (!c.moveToFirst()) {
                        IProgressNotifier notifier = new IProgressNotifier() {
                            @Override
                            public void notify(int p) {
                                Log.d(TAG, "progress:"+p);
                                publishProgress(p);
                            }
                        };
                        WordListManager.getInstance().loadWordListFromFile(WordListActivity.this,
                                InternalWordList.POSTGRADUATE_WORDLIST, true, notifier);
                        c.requery();
                    }
                    publishProgress(100);
                    break;

                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (type) {
                case INIT_QUERY:
                    Log.d(TAG, "update:" + values[0]);
                    progressBar.setProgress(values[0]);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (type) {
                case INIT_QUERY:
                    adapter = new WordListAdapter(WordListActivity.this, R.layout.word_list_item, c);
                    listView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                    findViewById(R.id.loadLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.view1).setVisibility(View.INVISIBLE);
                    // adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }
}
