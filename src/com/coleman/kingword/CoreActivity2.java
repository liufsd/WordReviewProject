
package com.coleman.kingword;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.skin.ColorManager;
import com.coleman.kingword.wordlist.AbsSubVisitor;
import com.coleman.kingword.wordlist.WordVisitor;
import com.coleman.log.Log;
import com.coleman.util.Config;
import com.coleman.util.DialogUtil;

public class CoreActivity2 extends Activity implements OnClickListener, OnScrollListener {

    private static final String TAG = CoreActivity2.class.getName();

    private static Log Log = Config.getLog();

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

    private Typeface mFace;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_list_2);
        listView = (ListView) findViewById(R.id.listView1);
        mFace = Typeface.createFromAsset(getAssets(), "font/seguibk.ttf");
        listView.setOnScrollListener(this);

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

    private void showOperation(int position) {
        final WordInfo info = subVisitor.getWordVisitor(position).info;
        final String arg0[] = new String[] {
                getString(R.string.new_word), getString(R.string.ignore)
        };
        final boolean arg1[] = new boolean[] {
                info.newword, info.ignore
        };
        DismissAlertDialog dialog = new DismissAlertDialog(this);
        dialog.setTitle(info.word);
        dialog.setButton(getString(R.string.ok),
                (android.content.DialogInterface.OnClickListener) null);
        View containView = getLayoutInflater().inflate(R.layout.core_2_dialog_list, null);
        ListView lv = (ListView) containView.findViewById(R.id.listView1);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                arg1[position] = !arg1[position];
                if (position == 0) {
                    info.newword = arg1[position];
                } else if (position == 1) {
                    info.ignore = arg1[position];
                }

                CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
                cb.setChecked(arg1[position]);

                WordInfoHelper.store(CoreActivity2.this, info);
                myAdapter.notifyDataSetChanged();
            }
        });
        class _Adapter extends BaseAdapter {
            private _Adapter() {
            }

            @Override
            public int getCount() {
                return arg0.length;
            }

            @Override
            public String getItem(int position) {
                return arg0[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.core_2_dialog_item, null);
                }
                TextView tv = (TextView) convertView.findViewById(R.id.textView1);
                CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
                tv.setText(arg0[position]);
                cb.setChecked(arg1[position]);
                return convertView;
            }

        }
        _Adapter adapter = new _Adapter();
        lv.setAdapter(adapter);
        dialog.setView(containView);
        dialog.show();
    }

    private void showDetail(int position) {
        final WordInfo info = subVisitor.getWordVisitor(position).info;
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        scrollView.addView(tv);

        String datas = subVisitor.getWordVisitor(position).getDetail(this).getDataAndSymbol();
        if (TextUtils.isEmpty(datas) || datas.equals(DictManager.WORD_NOT_FOUND)
                || datas.equals(DictManager.LIBRARY_NOT_INITIALED)) {
            datas = subVisitor.getWordVisitor(position).getDictData(this).getDataAndSymbol();
        }
        tv.setTypeface(mFace);
        tv.setText(Html.fromHtml(datas.replaceAll("\n", "<br/>")));
        // AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Dialog dialog = builder.setTitle(info.word).setView(scrollView)
        // .setPositiveButton(R.string.ok, null).create();
        DismissAlertDialog dialog = new DismissAlertDialog(this);
        dialog.setTitle(info.word);
        dialog.setView(scrollView);
        dialog.setButton(getString(R.string.ok),
                (android.content.DialogInterface.OnClickListener) null);
        dialog.show();
    }

    private void setReadMode() {
        findViewById(R.id.relativeLayout1).setBackgroundColor(
                ColorManager.getInstance().getBgColor());
    }

    private class MyAdapter extends BaseAdapter {
        private AbsSubVisitor asv;

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(CoreActivity2.this).inflate(R.layout.core_item_2,
                        null);
            }
            convertView.setBackgroundDrawable(ColorManager.getInstance().getSelector());

            LinearLayout layout1 = (LinearLayout) convertView.findViewById(R.id.linearLayout1);
            LinearLayout layout2 = (LinearLayout) convertView.findViewById(R.id.linearLayout2);
            TextView wordView = (TextView) convertView.findViewById(R.id.textView1);
            TextView datasView = (TextView) convertView.findViewById(R.id.textView2);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView1);

            int selectMode = ColorManager.getInstance().getSelectMode();
            int textColor = ColorManager.getInstance().getTextColor();
            wordView.setTextColor(textColor);
            datasView.setTextColor(textColor);

            layout1.setBackgroundDrawable(ColorManager.getInstance().getSelector());
            layout2.setBackgroundDrawable(ColorManager.getInstance().getSelector());
            if (selectMode == 1) {
                imgView.setBackgroundResource(R.drawable.word_night);
            } else {
                imgView.setBackgroundResource(R.drawable.word_day);
            }
            layout1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOperation(position);
                }
            });
            layout2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetail(position);
                }
            });

            datasView.setTypeface(mFace);
            String word = getItem(position).item.word;
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

            DictData datas = getItem(position).getData();
            String strDatas = datas == null ? "..." : datas.getDataAndSymbol();
            datasView.setText(Html.fromHtml(strDatas));
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
                    pd.show();
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

    private static class DismissAlertDialog extends AlertDialog {
        private Context mContext;

        protected DismissAlertDialog(Context context) {
            super(context);
            this.mContext = context;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            View view = getCurrentFocus();
            if (view == null || view instanceof ListView) {
                dismiss();
                DialogUtil.unregister(mContext, this);
            }
            return super.onTouchEvent(event);
        }

    }
}
