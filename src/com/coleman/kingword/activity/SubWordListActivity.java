
package com.coleman.kingword.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordsList;

public class SubWordListActivity extends Activity implements OnClickListener {
    Button[] btns = new Button[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_word_list_table);
        long wordlist_id = getIntent().getLongExtra(WordsList._ID, -1);
        Bundle b = new Bundle();
        b.putLong(WordsList._ID, wordlist_id);
        new CostTask().execute(b);
        initButtons();
        initHListView();
    }

    private void initHListView() {
        
    }

    private void initButtons() {
        btns[0] = (Button) findViewById(R.id.button1);
        btns[1] = (Button) findViewById(R.id.button2);
        btns[2] = (Button) findViewById(R.id.button3);

        btns[3] = (Button) findViewById(R.id.button4);
        btns[4] = (Button) findViewById(R.id.button5);
        btns[5] = (Button) findViewById(R.id.button6);

        btns[6] = (Button) findViewById(R.id.button7);
        btns[7] = (Button) findViewById(R.id.button8);
        btns[8] = (Button) findViewById(R.id.button9);

        btns[9] = (Button) findViewById(R.id.button10);
        btns[10] = (Button) findViewById(R.id.button11);
        btns[11] = (Button) findViewById(R.id.button12);

        int i = 0;
        for (Button btn : btns) {
            btn.setOnClickListener(this);
            i++;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
            case R.id.button2:
            case R.id.button3:
            case R.id.button4:
            case R.id.button5:
            case R.id.button6:
            case R.id.button7:
            case R.id.button8:
            case R.id.button9:
            case R.id.button10:
            case R.id.button11:
            case R.id.button12:
                Intent intent = new Intent(this, CoreActivity.class);
                int id = Integer.parseInt(((Button) v).getText().toString());
                intent.putExtra(SubWordsList._ID, id);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private class CostTask extends AsyncTask<Bundle, Void, Void> {
        private byte workType;

        private static final byte INIT_QUERY = 0;

        PageControl pageControl;

        @Override
        protected Void doInBackground(Bundle... params) {
            switch (workType) {
                case INIT_QUERY:
                    String projection[] = new String[] {
                        SubWordsList._ID
                    };
                    long wordlist_id = params[0].getLong(WordsList._ID);
                    Cursor c = getContentResolver().query(SubWordsList.CONTENT_URI, projection,
                            SubWordsList.WORD_LIST_ID + "=" + wordlist_id, null, null);
                    ArrayList<Long> list = new ArrayList<Long>();
                    if (c.moveToFirst()) {
                        while (!c.isAfterLast()) {
                            list.add(c.getLong(0));
                            c.moveToNext();
                        }
                    }
                    pageControl = new PageControl(list);
                    if (c != null) {
                        c.close();
                        c = null;
                    }
                    break;
                default:// ignore
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (workType) {
                case INIT_QUERY:
                    long[] sub_ids = pageControl.getPageInfo();
                    int i = 0;
                    for (Button btn : btns) {
                        if (i < sub_ids.length) {
                            btn.setText("" + sub_ids[i]);
                            btn.setVisibility(View.VISIBLE);
                        } else {
                            btn.setVisibility(View.GONE);
                        }
                        i++;
                    }
                    break;
                default:// ignore
                    break;
            }
        }
    }

    public static class PageControl {
        private ArrayList<long[]> mlist = new ArrayList<long[]>();

        private int curIndex;

        private final int MAX_PAGE_ITEM = 12;

        public PageControl(ArrayList<Long> list) {
            curIndex = 0;
            int size = list.size();
            int pnum = size / MAX_PAGE_ITEM;
            int lp = size % MAX_PAGE_ITEM;
            for (int i = 0; i < pnum; i++) {
                long[] page = new long[12];
                for (int j = 0; j < MAX_PAGE_ITEM; j++) {
                    page[j] = list.remove(0);
                }
                mlist.add(page);
            }
            if (lp != 0) {
                long[] page = new long[lp];
                for (int j = 0; j < lp; j++) {
                    page[j] = list.remove(0);
                }
                mlist.add(page);
            }
        }

        public void setCurPageIndex(int index) {
            this.curIndex = index;
        }

        /**
         * return an array of current page info.
         */
        public long[] getPageInfo() {
            return mlist.get(curIndex);
        }
    }
}
