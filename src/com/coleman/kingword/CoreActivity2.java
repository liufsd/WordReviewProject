
package com.coleman.kingword;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.skin.ColorManager;
import com.coleman.kingword.wordlist.AbsSubVisitor;
import com.coleman.kingword.wordlist.WordVisitor;
import com.coleman.log.Log;
import com.coleman.util.Config;
import com.coleman.util.DialogUtil;

public class CoreActivity2 extends Activity implements OnClickListener, OnScrollListener,
        OnItemClickListener, OnItemLongClickListener {

    private static final String TAG = CoreActivity2.class.getName();

    private Log Log = Config.getLog();

    private AbsSubVisitor subVisitor;

    private ListView listView;

    private MyAdapter myAdapter;

    private ProgressBar progressBar;

    /**
     * One of SCROLL_STATE_IDLE, SCROLL_STATE_TOUCH_SCROLL or
     * SCROLL_STATE_FLING.
     */
    private int scrollState = SCROLL_STATE_IDLE;

    private boolean startScroll;

    private ArrayList<ExpensiveTask> tasklist = new ArrayList<ExpensiveTask>();

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list_2);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        setReadMode();

        ExpensiveTask task = new ExpensiveTask(ExpensiveTask.TYPE_INIT);
        tasklist.add(task);
        task.execute();
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.core_option_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_study_mode:
                onBackPressed();
                break;
            case R.id.menu_all_complete:
                ExpensiveTask task = new ExpensiveTask(ExpensiveTask.TYPE_ALL_COMPLETE);
                task.execute();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                // @TODO
                break;
            default:
                break;
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (scrollState != SCROLL_STATE_IDLE) {
            startScroll = true;
        }
        if (startScroll && scrollState == SCROLL_STATE_IDLE) {
            startScroll = false;
            synchronized (tasklist) {
                for (int i = tasklist.size() - 1; i >= 0; i--) {
                    tasklist.remove(i).loading = false;
                }
            }
            ExpensiveTask task = new ExpensiveTask(ExpensiveTask.TYPE_LOADING);
            tasklist.add(task);
            task.execute(firstVisibleItem);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "===coleman-debug-item click:" + subVisitor.getWordVisitor(position).info.word);
        final WordInfo info = subVisitor.getWordVisitor(position).info;
        String arg0[] = new String[] {
                getString(R.string.new_word), getString(R.string.ignore)
        };
        boolean arg1[] = new boolean[] {
                info.newword, info.ignore
        };
        DialogInterface.OnMultiChoiceClickListener arg2 = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch (which) {
                    case 0:
                        info.newword = isChecked;
                        WordInfoHelper.store(CoreActivity2.this, info);
                        myAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        info.ignore = isChecked;
                        WordInfoHelper.store(CoreActivity2.this, info);
                        myAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
        Dialog dialog = new AlertDialog.Builder(this).setTitle(info.word)
                .setMultiChoiceItems(arg0, arg1, arg2)
                .setPositiveButton(getString(R.string.ok), null).create();
        DialogUtil.showDialog(this, dialog);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final WordInfo info = subVisitor.getWordVisitor(position).info;
        DialogUtil.showMessage(this, info.word,
                subVisitor.getWordVisitor(position).getDictData(this).getDataAndSymbol(),
                getString(R.string.ok), null, null, null);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("sub", subVisitor);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onDestroy() {
        synchronized (tasklist) {
            for (int i = tasklist.size() - 1; i >= 0; i--) {
                ExpensiveTask task = tasklist.remove(i);
                task.loading = false;
                task.resetState = false;
            }
        }
        super.onDestroy();
    }

    private void setReadMode() {
        findViewById(R.id.relativeLayout1).setBackgroundColor(
                ColorManager.getInstance().getBgColor());
    }

    private class MyAdapter extends BaseAdapter {
        private AbsSubVisitor asv;

        Typeface mFace = Typeface.createFromAsset(getAssets(), "font/seguibk.ttf");

        private MyAdapter(AbsSubVisitor subVisitor) {
            asv = subVisitor;
        }

        @Override
        public int getCount() {
            return asv.getCount();
        }

        @Override
        public WordVisitor getItem(int position) {
            return asv.getWordVisitor(position);
        }

        @Override
        public long getItemId(int position) {
            return asv.getWordVisitor(position).info.id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(CoreActivity2.this).inflate(R.layout.core_item_2,
                        null);
            }
            convertView.setBackgroundDrawable(ColorManager.getInstance().getSelector());

            TextView wordView = (TextView) convertView.findViewById(R.id.textView1);
            TextView datasView = (TextView) convertView.findViewById(R.id.textView2);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView1);

            int selectMode = ColorManager.getInstance().getSelectMode();
            if (selectMode == 1) {
                imgView.setBackgroundResource(R.drawable.word_night);
            } else {
                imgView.setBackgroundResource(R.drawable.word_day);
            }

            datasView.setTypeface(mFace);
            String word = getItem(position).item.word;
            DictData datas = getItem(position).getData();
            String strDatas = datas == null ? "..." : datas.getDataAndSymbol()
                    .replaceAll("\n", " ");
            SpannableString sp = new SpannableString(word);
            if (getItem(position).info.newword) {
                sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, sp.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (getItem(position).info.ignore) {
                sp.setSpan(new StrikethroughSpan(), 0, sp.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            wordView.setText(sp);
            sp = new SpannableString(strDatas);
            datasView.setText(sp);
            wordView.setTextColor(ColorManager.getInstance().getTextColor());
            datasView.setTextColor(ColorManager.getInstance().getTextColor());
            return convertView;
        }
    }

    private class ExpensiveTask extends AsyncTask<Integer, Integer, Void> implements
            OnCancelListener {
        public static final int TYPE_INIT = 1;

        public static final int TYPE_LOADING = 2;

        public static final int TYPE_ALL_COMPLETE = 3;

        private int type;

        private boolean loading;

        private boolean resetState;

        private ProgressDialog pd;

        public ExpensiveTask(int type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case TYPE_INIT:
                    if (ColorManager.getInstance().getSelectMode() == 0) {
                        progressBar = (ProgressBar) findViewById(R.id.progressBar1Day);
                    } else {
                        progressBar = (ProgressBar) findViewById(R.id.progressBar1Night);
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case TYPE_LOADING:
                    loading = true;
                    break;
                case TYPE_ALL_COMPLETE:
                    resetState = true;
                    pd = new ProgressDialog(CoreActivity2.this);
                    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pd.setMax(subVisitor.getCount());
                    pd.setOnCancelListener(this);
                    pd.setMessage(getString(R.string.view_state_reset));
                    DialogUtil.showDialog(CoreActivity2.this, pd);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            switch (type) {
                case TYPE_INIT:
                    Intent intent = getIntent();
                    subVisitor = (AbsSubVisitor) intent.getSerializableExtra("sub");
                    break;
                case TYPE_LOADING:
                    int sp = 0;
                    if (params != null && params.length > 0) {
                        sp = params[0];
                    }
                    for (int i = sp; i < subVisitor.getCount() + sp && loading; i++) {
                        subVisitor.getWordVisitor(i % subVisitor.getCount()).getDictData(
                                CoreActivity2.this);
                        publishProgress();
                    }
                    break;
                case TYPE_ALL_COMPLETE:
                    boolean isComplete = false;
                    for (int i = 0; resetState && i < subVisitor.getCount(); i++) {
                        WordVisitor wv = subVisitor.getWordVisitor(i);
                        isComplete = wv.isComplete();
                        while (!wv.isComplete() && resetState) {
                            wv.setPass(true);
                        }
                        if (!isComplete) {
                            wv.viewPlus(CoreActivity2.this);
                        }
                        publishProgress(i + 1);
                    }
                    break;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (isFinishing()) {
                return;
            }
            switch (type) {
                case TYPE_INIT:
                    break;
                case TYPE_LOADING:
                    myAdapter.notifyDataSetChanged();
                    break;
                case TYPE_ALL_COMPLETE:
                    pd.setProgress(values[0]);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isFinishing()) {
                return;
            }
            switch (type) {
                case TYPE_INIT:
                    progressBar.setVisibility(View.GONE);
                    myAdapter = new MyAdapter(subVisitor);
                    listView.setAdapter(myAdapter);

                    ExpensiveTask task = new ExpensiveTask(TYPE_LOADING);
                    tasklist.add(task);
                    task.execute();
                    break;
                case TYPE_LOADING:
                    synchronized (tasklist) {
                        loading = false;
                        tasklist.remove(this);
                    }
                    break;
                case TYPE_ALL_COMPLETE:
                    pd.dismiss();
                    if (subVisitor.allComplete()) {
                        onBackPressed();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (dialog.equals(pd)) {
                resetState = false;
            }
        }

    }
}
