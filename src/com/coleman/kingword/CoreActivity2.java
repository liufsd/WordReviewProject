
package com.coleman.kingword;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.wordlist.AbsSubVisitor;
import com.coleman.kingword.wordlist.WordVisitor;
import com.coleman.util.AppSettings;

public class CoreActivity2 extends Activity implements OnClickListener, OnScrollListener {

    private AbsSubVisitor subVisitor;

    private ListView listView;

    private MyAdapter myAdapter;

    private ProgressBar progressBar;

    private int selectMode;// 0 day 1 night 2 custom

    private int textColor, bgColor, selectColor;

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
                finish();
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
    protected void onDestroy() {
        synchronized (tasklist) {
            for (int i = tasklist.size() - 1; i >= 0; i--) {
                tasklist.remove(i).loading = false;
            }
        }
        super.onDestroy();
    }

    private void setColorMode() {
        selectMode = AppSettings.getInt(AppSettings.SELECT_COLOR_MODE_KEY, 0);
        textColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][0], Color.BLACK);
        bgColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][1], Color.WHITE);
        selectColor = AppSettings.getInt(AppSettings.COLOR_MODE[selectMode][2], Color.GRAY);
    }

    private class BGDrawable extends StateListDrawable {
        public BGDrawable() {
            addState(new int[] {
                android.R.attr.state_pressed
            }, new ColorDrawable(selectColor));
            addState(new int[] {
                android.R.attr.state_selected
            }, new ColorDrawable(selectColor));
            addState(new int[] {
                -android.R.attr.state_selected
            }, new ColorDrawable(bgColor));
        }
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
            BGDrawable bg = new BGDrawable();
            convertView.setBackgroundDrawable(bg);

            TextView wordView = (TextView) convertView.findViewById(R.id.textView1);
            TextView datasView = (TextView) convertView.findViewById(R.id.textView2);

            datasView.setTypeface(mFace);
            String word = getItem(position).item.word;
            DictData datas = getItem(position).getData();
            String strDatas = datas == null ? "..." : datas.getDataAndSymbol()
                    .replaceAll("\n", " ");
            wordView.setText(word);
            datasView.setText(strDatas);
            wordView.setTextColor(textColor);
            datasView.setTextColor(textColor);
            return convertView;
        }

    }

    private class ExpensiveTask extends AsyncTask<Integer, Void, Void> {
        public static final int TYPE_INIT = 1;

        public static final int TYPE_LOADING = 2;

        private int type;

        private boolean loading;

        public ExpensiveTask(int type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case TYPE_INIT:
                    setColorMode();
                    if (selectMode == 0) {
                        progressBar = (ProgressBar) findViewById(R.id.progressBar1Day);
                    } else {
                        progressBar = (ProgressBar) findViewById(R.id.progressBar1Night);
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case TYPE_LOADING:
                    loading = true;
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
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (isFinishing()) {
                return;
            }
            switch (type) {
                case TYPE_INIT:
                    break;
                case TYPE_LOADING:
                    myAdapter.notifyDataSetChanged();
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
                default:
                    break;
            }
        }

    }
}
