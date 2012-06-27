
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
import android.widget.TextView;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.kingword.wordlist.view.PageBottomBar;
import com.coleman.kingword.wordlist.view.ScrollLayout;
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
        initQuery(wordlist_id);
    }

    private void initQuery(long id) {
        String projection[] = new String[] {
                TSubWordList._ID, TSubWordList.LEVEL, TSubWordList.METHOD, TSubWordList.POSITION
        };
        long wordlist_id = id;
        Cursor c = MyApp.context.getContentResolver().query(TSubWordList.CONTENT_URI, projection,
                TSubWordList.WORD_LIST_ID + "=" + wordlist_id, null, null);
        ArrayList<SubWordList> list = new ArrayList<SubWordList>();
        int i = 1;
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                SubWordList swl = new SubWordList(wordlist_id);
                swl.id = c.getLong(0);
                swl.level = c.getInt(1);
                swl.index = i;
                swl.method = c.getString(2);
                swl.itemIndexInLoop = c.getInt(3);
                list.add(swl);
                c.moveToNext();
                i++;
            }
        }
        new PageControl(list).compute();
        if (c != null) {
            c.close();
            c = null;
        }
    }

    private class PageControl {
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
                gv.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!mScrollLayout.moveEvent()) {
                            Intent intent = new Intent(SubListActivity.this, CoreActivity.class);
                            intent.putExtra("type", SubWordListAccessor.SUB_WORD_LIST);
                            SubWordList info = (SubWordList) parent.getItemAtPosition(position);
                            intent.putExtra("subinfo", (Parcelable) info);
                            SubListActivity.this.startActivity(intent);
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
            TextView title = (TextView) convertView.findViewById(R.id.textView1);
            TextView subTitle = (TextView) convertView.findViewById(R.id.textView2);
            title.setText("unit " + getItem(position).index);
            subTitle.setText("loop " + getItem(position).itemIndexInLoop);
            return convertView;
        }

    }
}
