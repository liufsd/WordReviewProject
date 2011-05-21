
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
import android.widget.ProgressBar;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordsList;

public class SubWordListActivity extends Activity implements OnClickListener {
    Button[] btns = new Button[12];

    private ProgressBar progressBar;

    /**
     * Loading when first entering.
     */
    private ProgressBar preProgress;

    /**
     * This view do nothing, just to occupy the residual space.
     */
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_word_list_table);
        long wordlist_id = getIntent().getLongExtra(WordsList._ID, -1);
        Bundle b = new Bundle();
        b.putLong(WordsList._ID, wordlist_id);
        initViews();
        new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute(b);
    }

    private void initViews() {
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
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        preProgress = (ProgressBar) findViewById(R.id.progressBar2);
        emptyView = findViewById(R.id.view1);
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

    private class ExpensiveTask extends AsyncTask<Bundle, Void, Void> {
        private byte type;

        private static final byte INIT_QUERY = 0;

        PageControl pageControl;

        public ExpensiveTask(byte type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case INIT_QUERY:
                    for (Button btn : btns) {
                        btn.setVisibility(View.GONE);
                    }
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    preProgress.setVisibility(View.VISIBLE);
                    break;
                default:// ignore
                    break;
            }
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            switch (type) {
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
            switch (type) {
                case INIT_QUERY:
                    for (Button btn : btns) {
                        btn.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.VISIBLE);
                    preProgress.setVisibility(View.GONE);
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
                    progressBar.setProgress(pageControl.getProgress());
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

        public int getProgress() {
            if (mlist.size() == 0) {
                return 0;
            }
            return (curIndex + 1) * 100 / mlist.size();
        }

        public int getCurPageIndex() {
            return curIndex;
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
