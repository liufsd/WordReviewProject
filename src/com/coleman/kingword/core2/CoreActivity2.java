
package com.coleman.kingword.core2;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.wordlist.AbsSubVisitor;
import com.coleman.kingword.wordlist.WordVisitor;
import com.coleman.util.AppSettings;
import com.coleman.util.DialogUtil;

public class CoreActivity2 extends Activity implements OnClickListener, OnScrollListener,
        OnItemClickListener, OnItemLongClickListener {

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
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

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
                        break;
                    case 1:
                        info.ignore = isChecked;
                        WordInfoHelper.store(CoreActivity2.this, info);
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(info.word).setMultiChoiceItems(arg0, arg1, arg2)
                .setPositiveButton(getString(R.string.ok), null).show();
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

    private class MyAdapter extends BaseAdapter implements OnClickListener {
        private AbsSubVisitor asv;

        private HashMap<Integer, Boolean> map = new HashMap<Integer, Boolean>();

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
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView1);
            imgView.setOnClickListener(this);
            imgView.setTag(position);

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

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageView1:
                    if (!map.containsKey(v.getTag()) || !map.get(v.getTag())) {
                        map.put((Integer) v.getTag(), true);
                    } else {
                        map.put((Integer) v.getTag(), false);
                    }
                    myAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
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
