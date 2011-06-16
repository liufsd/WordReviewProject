
package com.coleman.kingword.activity;

import java.io.File;

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

    public static final String EXTERNAL_FILE = "external_file";

    public static final String EXTERNAL_FILE_PATH = "external_file_path";

    private String external_file_path;

    private ListView listView;

    private Button loadBtn;

    private ProgressBar progressBar;

    private View loadLayout;

    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list);
        loadLayout = findViewById(R.id.loadLayout);
        emptyView = findViewById(R.id.view1);
        listView = (ListView) findViewById(R.id.listView1);
        loadBtn = (Button) findViewById(R.id.button1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        listView.setOnItemClickListener(this);
        loadBtn.setOnClickListener(this);
        boolean loadExternal = getIntent().getBooleanExtra(EXTERNAL_FILE, false);
        if (loadExternal) {
            external_file_path = getIntent().getStringExtra(EXTERNAL_FILE_PATH);
            new ExpensiveTask(ExpensiveTask.LOAD_EXTERNAL).execute();
        } else {
            new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if (c != null) {
            c.close();
            c = null;
        }
        super.onDestroy();
    }

    private class WordListAdapter extends ResourceCursorAdapter {
        public WordListAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            view.setTag(cursor.getLong(0));
            TextView tv = (TextView) view.findViewById(R.id.textView1);
            String text = getName(cursor.getString(2));
            tv.setText(text);
            tv.setLines(3);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return super.newView(context, cursor, parent);
        }

        private String getName(String src) {
            String str = (src == null ? "" : src);
            int s_idx = src.lastIndexOf(File.separator);
            if (s_idx != -1 && s_idx < src.length() - 1) {
                str = str.substring(s_idx + 1);
            }
            int e_idx = str.lastIndexOf(".");
            if (e_idx != -1) {
                str = str.substring(0, e_idx);
            }
            return str;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(WordListActivity.this, SubWordListActivity.class);
        i.putExtra(WordsList._ID, (Long) view.getTag());
        startActivity(i);
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

        private static final byte LOAD_EXTERNAL = 1;

        public ExpensiveTask(byte initQuery) {
            this.type = initQuery;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case INIT_QUERY:
                    progressBar.setVisibility(View.VISIBLE);
                    loadLayout.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_EXTERNAL:
                    listView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    loadLayout.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (type) {
                case INIT_QUERY: {
                    c = getContentResolver().query(WordsList.CONTENT_URI, projection, null, null,
                            null);
                    if (!c.moveToFirst()) {
                        IProgressNotifier notifier = new IProgressNotifier() {
                            @Override
                            public void notify(int p) {
                                Log.d(TAG, "progress:" + p);
                                publishProgress(p);
                            }
                        };
                        WordListManager.getInstance().loadWordListFromAsset(WordListActivity.this,
                                InternalWordList.POSTGRADUATE_WORDLIST, notifier);
                        c.requery();
                    }
                    publishProgress(100);
                    break;
                }
                case LOAD_EXTERNAL: {
                    c = getContentResolver().query(WordsList.CONTENT_URI, projection, null, null,
                            null);
                    IProgressNotifier notifier = new IProgressNotifier() {
                        @Override
                        public void notify(int p) {
                            Log.d(TAG, "progress:" + p);
                            publishProgress(p);
                        }
                    };
                    WordListManager.getInstance().loadWordListFromFile(WordListActivity.this,
                            external_file_path, notifier);
                    c.requery();
                    publishProgress(100);
                    break;
                }
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
                case LOAD_EXTERNAL:
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
                    loadLayout.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.INVISIBLE);
                    break;
                case LOAD_EXTERNAL:
                    listView.setVisibility(View.VISIBLE);
                    adapter = new WordListAdapter(WordListActivity.this, R.layout.word_list_item, c);
                    listView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                    loadLayout.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    }
}
