
package com.coleman.kingword.wordlist;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.wordlist.WordListManager.IProgressNotifier;
import com.coleman.kingword.wordlist.model.WordList.InternalWordList;
import com.coleman.log.Log;
import com.coleman.util.Config;

public class WordListActivity extends Activity implements OnItemClickListener, OnClickListener,
        OnMenuItemClickListener {
    private static final String TAG = WordListActivity.class.getName();

    private static Log Log = Config.getLog();

    private WordListAdapter adapter;

    private Cursor c;

    private static final String projection[] = new String[] {
            TWordList._ID, TWordList.DESCRIBE, TWordList.PATH_NAME, TWordList.SET_METHOD
    };

    public static final String EXTERNAL_FILE = "external_file";

    public static final String EXTERNAL_FILE_PATH = "external_file_path";

    private String external_file_path;

    private ListView listView;

    private Button btnNew, btnIgnore;

    private ProgressBar progressBar;

    private View loadLayout;

    private View emptyView;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list);
        loadLayout = findViewById(R.id.loadLayout);
        emptyView = findViewById(R.id.view1);
        listView = (ListView) findViewById(R.id.listView1);
        btnNew = (Button) findViewById(R.id.button1);
        btnIgnore = (Button) findViewById(R.id.button2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        btnNew.setOnClickListener(this);
        btnIgnore.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean loadExternal = getIntent().getBooleanExtra(EXTERNAL_FILE, false);
        getIntent().removeExtra(EXTERNAL_FILE);
        if (loadExternal) {
            external_file_path = getIntent().getStringExtra(EXTERNAL_FILE_PATH);
            getIntent().removeExtra(EXTERNAL_FILE_PATH);
            Log.d(TAG, "external_file_path:" + external_file_path);
            new ExpensiveTask(ExpensiveTask.LOAD_EXTERNAL).execute();
        } else {
            if (c != null && !c.isClosed()) {
                c.requery();
            } else {
                new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute();
            }
        }
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
        if (!DictManager.getInstance().isCurLibInitialized()) {
            showDBInitHint();
        } else {
            Intent i = new Intent(WordListActivity.this, SubListActivity.class);
            i.putExtra(TWordList._ID, (Long) view.getTag());
            startActivity(i);
        }
    }

    private void showDBInitHint() {
        new AlertDialog.Builder(this).setTitle(R.string.msg_dialog_title)
                .setMessage(R.string.init_db).setPositiveButton(R.string.ok, null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.wordlist_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch (item.getItemId()) {
        // case R.id.menu_load:
        // startActivity(new Intent(this, FileExplorer.class));
        // break;
        // case R.id.menu_ignore_list:
        // Intent intent = new Intent(this, CoreActivity.class);
        // intent.putExtra("type", WordListAccessor.SCAN_LIST);
        // startActivity(intent);
        // break;
        // }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.option);
        getMenuInflater().inflate(R.menu.wordlist_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        TextView tv = (TextView) info.targetView.findViewById(R.id.textView1);
        switch (item.getItemId()) {
            case R.id.menu_rename:
                showRenameDialog(info.id);
                break;
            case R.id.menu_delete:
                showDeleteDialog(info.id, tv.getText().toString());
                break;
            default:
                break;
        }
        Log.d(TAG, "info position:" + info.position);
        Log.d(TAG, "info id: " + info.id);
        Log.d(TAG, "item db id: " + adapter.getItemId(info.position));
        return true;
    }

    private void showDeleteDialog(final long id, String name) {
        String msg = String.format(getString(R.string.delete_warning), name);
        new AlertDialog.Builder(this).setTitle(R.string.warning_dialog_title).setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(TWordList.CONTENT_URI,
                                TWordList._ID + " = " + id, null);
                        c.requery();
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void showRenameDialog(final long id) {
        View view = LayoutInflater.from(this).inflate(R.layout.input_dialog, null);
        final EditText et = (EditText) view.findViewById(R.id.editText1);
        new AlertDialog.Builder(this).setTitle(R.string.input_dialog_title).setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = et.getText().toString();
                        if (!TextUtils.isEmpty(str)) {
                            ContentValues value = new ContentValues();
                            value.put(TWordList.PATH_NAME, str);
                            getContentResolver().update(TWordList.CONTENT_URI, value,
                                    TWordList._ID + " = " + id, null);
                            c.requery();
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                if (!DictManager.getInstance().isCurLibInitialized()) {
                    showDBInitHint();
                } else if (!WordInfoHelper.hasWordInfo(this, SubWordListAccessor.NEW_WORD_BOOK_LIST)) {
                    showNoNewWordHint();
                } else {
                    Intent intent = new Intent(this, CoreActivity.class);
                    intent.putExtra("type", SubWordListAccessor.NEW_WORD_BOOK_LIST);
                    startActivity(intent);
                }
                break;
            case R.id.button2:
                if (!DictManager.getInstance().isCurLibInitialized()) {
                    showDBInitHint();
                } else if (!WordInfoHelper.hasWordInfo(this, SubWordListAccessor.SCAN_LIST)) {
                    showNoIgnoreWordHint();
                } else {
                    Intent intent = new Intent(this, CoreActivity.class);
                    intent.putExtra("type", SubWordListAccessor.SCAN_LIST);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    private void showNoNewWordHint() {
        new AlertDialog.Builder(this).setTitle(R.string.view_new_book)
                .setMessage(R.string.no_new_word_found).setPositiveButton(R.string.ok, null).show();
    }

    private void showNoIgnoreWordHint() {
        new AlertDialog.Builder(this).setTitle(R.string.view_ignore_list)
                .setMessage(R.string.no_ignore_word_found).setPositiveButton(R.string.ok, null)
                .show();
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
                    listView.setVisibility(View.GONE);
                    loadLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_EXTERNAL:
                    listView.setVisibility(View.GONE);
                    loadLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
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
                    c = getContentResolver().query(TWordList.CONTENT_URI, projection, null, null,
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
                    break;
                }
                case LOAD_EXTERNAL: {
                    c = getContentResolver().query(TWordList.CONTENT_URI, projection, null, null,
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
                    if (progressBar.getVisibility() != View.VISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(values[0]);
                    break;
                case LOAD_EXTERNAL:

                    if (progressBar.getVisibility() != View.VISIBLE) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
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
                    listView.setVisibility(View.VISIBLE);
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
