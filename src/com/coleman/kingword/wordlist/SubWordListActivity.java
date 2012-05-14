
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.kingword.wordlist.view.SlideTableSwitcher;

public class SubWordListActivity extends Activity {

    private static final String TAG = "SubWordListActivity";

    private ProgressBar progressBar;

    /**
     * Loading when first entering.
     */
    private ProgressBar preProgress;

    private SlideTableSwitcher mSwitcher;

    private PageControl pageControl;

    private long wordlist_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_word_list_table);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle b = new Bundle();
        if (wordlist_id == -1) {
            wordlist_id = getIntent().getLongExtra(TWordList._ID, -1);
        }
        b.putLong(TWordList._ID, wordlist_id);
        new ExpensiveTask(ExpensiveTask.INIT_QUERY).execute(b);
    }

    private void initViews() {
        mSwitcher = (SlideTableSwitcher) findViewById(R.id.viewSwitcher1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        preProgress = (ProgressBar) findViewById(R.id.progressBar2);
    }

    private class ExpensiveTask extends AsyncTask<Bundle, Void, Void> {
        private byte type;

        private static final byte INIT_QUERY = 0;

        public ExpensiveTask(byte type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case INIT_QUERY:
                    progressBar.setVisibility(View.GONE);
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
                            TSubWordList._ID, TSubWordList.LEVEL
                    };
                    long wordlist_id = params[0].getLong(TWordList._ID);
                    Cursor c = getContentResolver().query(TSubWordList.CONTENT_URI, projection,
                            TSubWordList.WORD_LIST_ID + "=" + wordlist_id, null, null);
                    ArrayList<SubWordList> list = new ArrayList<SubWordList>();
                    int i = 1;
                    if (c.moveToFirst()) {
                        while (!c.isAfterLast()) {
                            list.add(new SubWordList(c.getLong(0), i, c.getInt(1), wordlist_id));
                            c.moveToNext();
                            i++;
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
                    progressBar.setVisibility(View.VISIBLE);
                    preProgress.setVisibility(View.GONE);
                    SubWordList[] sub_ids = pageControl.getPageInfo();
                    mSwitcher.showCurrentScreen(sub_ids);
                    progressBar.setProgress(pageControl.getProgress());
                    break;
                default:// ignore
                    break;
            }
        }
    }

    public static class PageControl {
        private ArrayList<SubWordList[]> mlist = new ArrayList<SubWordList[]>();

        private int curIndex;

        private final int MAX_PAGE_ITEM = 12;

        public PageControl(ArrayList<SubWordList> list) {
            curIndex = 0;
            int size = list.size();
            int pnum = size / MAX_PAGE_ITEM;
            int lp = size % MAX_PAGE_ITEM;
            for (int i = 0; i < pnum; i++) {
                SubWordList[] page = new SubWordList[12];
                for (int j = 0; j < MAX_PAGE_ITEM; j++) {
                    page[j] = list.remove(0);
                }
                mlist.add(page);
            }
            if (lp != 0) {
                SubWordList[] page = new SubWordList[lp];
                for (int j = 0; j < lp; j++) {
                    page[j] = list.remove(0);
                }
                mlist.add(page);
            }
        }

        public int getProgress() {
            if (mlist.size() == 0) {
                return 0;
            } else if (mlist.size() == 1) {
                return 100;
            }
            return curIndex * 100 / (mlist.size() - 1);
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
        public SubWordList[] getPageInfo() {
            return mlist.get(curIndex);
        }

        public boolean hasNextPage() {
            return curIndex + 1 <= mlist.size() - 1;
        }

        public boolean hasPreviousPage() {
            return curIndex - 1 >= 0;
        }

        public SubWordList[] moveToNextPage() {
            curIndex = curIndex + 1 > mlist.size() - 1 ? mlist.size() - 1 : curIndex + 1;
            return mlist.get(curIndex);
        }

        public SubWordList[] moveToPrePage() {
            curIndex = curIndex - 1 < 0 ? 0 : curIndex - 1;
            return mlist.get(curIndex);
        }

    }

    int d_x = 0;

    int u_x = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            d_x = (int) event.getX();
        } else if (MotionEvent.ACTION_UP == event.getAction()) {
            u_x = (int) event.getX();
            if (u_x - d_x > getWindowManager().getDefaultDisplay().getWidth() / 5) {
                if (pageControl.hasPreviousPage()) {
                    SubWordList sub_ids[] = pageControl.moveToPrePage();
                    mSwitcher.showPreviousScreen(sub_ids);
                    progressBar.setProgress(pageControl.getProgress());
                }
            } else if (d_x - u_x > getWindowManager().getDefaultDisplay().getWidth() / 5) {
                if (pageControl.hasNextPage()) {
                    SubWordList sub_ids[] = pageControl.moveToNextPage();
                    mSwitcher.showNextScreen(sub_ids);
                    progressBar.setProgress(pageControl.getProgress());
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
