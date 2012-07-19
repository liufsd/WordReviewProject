
package com.coleman.kingword.wordlist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.kingword.wordlist.view.PageBottomBar;
import com.coleman.kingword.wordlist.view.ScrollLayout;
import com.coleman.log.Log;
import com.coleman.util.Config;
import com.coleman.util.MyApp;

/**
 * 1.查询数据库有多少单元
 * <p>
 * 2.计算出这些单元需要分成多少屏
 * <p>
 * 3.添加这些屏幕到ScrollLayout中
 * 
 * @author coleman
 * @version [version, Jun 26, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
public class SubListActivity extends Activity {

    public static final String SCREEN_INDEX = "screen_index";

    private ScrollLayout mScrollLayout;

    private PageBottomBar mPageBottomBar;

    private long wordlist_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding);
        mScrollLayout = (ScrollLayout) findViewById(R.id.scrollLayout1);
        mPageBottomBar = (PageBottomBar) findViewById(R.id.pageBottomBar1);
        if (wordlist_id == -1) {
            wordlist_id = getIntent().getLongExtra(TWordList._ID, -1);
        }
        int curScreen = getIntent().getIntExtra(SCREEN_INDEX, ScrollLayout.DEFAULT_SCREEN);
        mScrollLayout.setCurrentScreen(curScreen);
        initQuery(wordlist_id);
    }

    private void initQuery(long id) {
        String projection[] = new String[] {
                TSubWordList._ID, TSubWordList.HISTORY_LEVEL, TSubWordList.COUNT_DOWN,
                TSubWordList.LEVEL, TSubWordList.METHOD, TSubWordList.POSITION,
                TSubWordList.ERROR_COUNT, TSubWordList.PROGRESS
        };
        long wordlist_id = id;
        Cursor c = MyApp.context.getContentResolver().query(TSubWordList.CONTENT_URI, projection,
                TSubWordList.WORD_LIST_ID + "=" + wordlist_id, null, null);
        ArrayList<SubWordList> list = new ArrayList<SubWordList>();
        int i = 1;
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                SubWordList swl = new SubWordList(wordlist_id);
                swl.id = c.getLong(c.getColumnIndex(TSubWordList._ID));
                swl.history_level = c.getInt(c.getColumnIndex(TSubWordList.HISTORY_LEVEL));
                swl.count_down = c.getInt(c.getColumnIndex(TSubWordList.COUNT_DOWN));
                swl.level = c.getInt(c.getColumnIndex(TSubWordList.LEVEL));
                swl.index = i;
                swl.method = c.getString(c.getColumnIndex(TSubWordList.METHOD));
                swl.position = c.getInt(c.getColumnIndex(TSubWordList.POSITION));
                swl.error_count = c.getInt(c.getColumnIndex(TSubWordList.ERROR_COUNT));
                swl.progress = c.getInt(c.getColumnIndex(TSubWordList.PROGRESS));
                list.add(swl);
                c.moveToNext();
                i++;
            }
        }
        PageControl pc = new PageControl(list);
        pc.compute();
        if (c != null) {
            c.close();
            c = null;
        }
    }

    private class PageControl {

        protected final String TAG = PageControl.class.getName();

        protected final Log Log = Config.getLog();

        private int pageItems = 12;

        private int pages = 0;

        private ArrayList<SubWordList> list;

        public PageControl(ArrayList<SubWordList> list) {
            this.list = list;
        }

        public void compute() {
            int temp = list.size() % pageItems;
            pages = list.size() / pageItems;
            pages = temp > 0 ? pages + 1 : pages;
            LayoutInflater inf = LayoutInflater.from(SubListActivity.this);
            for (int i = 0; i < pages; i++) {
                View view = inf.inflate(R.layout.sliding_screen, null);
                GridView gv = (GridView) view.findViewById(R.id.gridView1);
                int s = i * pageItems;
                int e = i * pageItems + pageItems;
                e = e > list.size() ? list.size() : e;
                PageAdapter adapter = new PageAdapter(list.subList(s, e));
                gv.setAdapter(adapter);
                mScrollLayout.addView(view);

                final int si = i;
                gv.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!mScrollLayout.moveEvent()) {
                            Intent intent = new Intent(SubListActivity.this, CoreActivity.class);
                            intent.putExtra("type", SubWordListAccessor.SUB_WORD_LIST);
                            SubWordList info = (SubWordList) parent.getItemAtPosition(position);
                            Log.i(TAG, "===coleman-debug-subinfo: " + info.error_count);
                            info.screenIndex = si;
                            intent.putExtra("subinfo", (Parcelable) info);
                            SubListActivity.this.startActivity(intent);
                            finish();
                        }
                    }
                });
                View view2 = inf.inflate(R.layout.sliding_btm_item, null);
                mPageBottomBar.addView(view2);
            }
            mScrollLayout.setBottomBar(mPageBottomBar);
            mScrollLayout.invalidate();
        }

    }

    private class PageAdapter extends BaseAdapter {
        private List<SubWordList> list;

        private final int[] historyRateLev = new int[] {
                R.drawable.rate0, R.drawable.rate1, R.drawable.rate2, R.drawable.rate3,
                R.drawable.rate4, R.drawable.rate5
        };

        private final int[] historyPaperLev = new int[] {
                R.drawable.unit0, R.drawable.unit1, R.drawable.unit2, R.drawable.unit3,
                R.drawable.unit4, R.drawable.unit5
        };

        public PageAdapter(List<SubWordList> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public SubWordList getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SubListActivity.this).inflate(
                        R.layout.sliding_screen_item, null);
            }
            ImageView ivPaper = (ImageView) convertView.findViewById(R.id.imageView1);
            ivPaper.setImageResource(historyPaperLev[getItem(position).history_level + 1]);

            ImageView ivRate = (ImageView) convertView.findViewById(R.id.imageView2);
            ivRate.setImageResource(historyRateLev[getItem(position).history_level + 1]);

            TextView title = (TextView) convertView.findViewById(R.id.textView1);
            title.setText("unit " + getItem(position).index);

            TextView subTitle = (TextView) convertView.findViewById(R.id.textView2);
            subTitle.setText(SubWordListAccessor.getLevelStrings(SubListActivity.this,
                    getItem(position).level));

            return convertView;
        }

    }
}
