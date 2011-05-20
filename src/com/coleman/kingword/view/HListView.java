
package com.coleman.kingword.view;

import com.coleman.kingword.R;

import android.content.Context;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Toast;

public class HListView extends FrameLayout {
    private static final String TAG = HListView.class.getName();

    LinearLayout linBg;

    LinearLayout linCur;

    ImageView cursorView;

    int screenWidth;

    final int ITEM_W = 60;

    final int ITEM_H = 51;

    float bg_off_x;

    float bg_off_y;

    int select;

    private int items_num = 3;

    int[] menus = new int[items_num];

    private FlingRunnable mCurFling = new FlingRunnable();

    public HListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        cursorView = new ImageView(getContext()) {
            @Override
            public void onMeasure(int width, int height) {
                super.onMeasure(MeasureSpec.EXACTLY | ITEM_W, MeasureSpec.EXACTLY | ITEM_H);
            }
        };
        cursorView.setImageResource(R.drawable.page_selected);
        linBg = new LinearLayout(getContext());
        linBg.setOrientation(LinearLayout.HORIZONTAL);
        linCur = new LinearLayout(getContext());
        linCur.setOrientation(LinearLayout.HORIZONTAL);
        linCur.addView(cursorView);
        for (int i = 0; i < items_num; i++) {
            final ImageView imgv = new ImageView(getContext()) {
                @Override
                public void onMeasure(int width, int height) {
                    super.onMeasure(MeasureSpec.EXACTLY | ITEM_W, MeasureSpec.EXACTLY | ITEM_H);
                }
            };
            imgv.setImageResource(R.drawable.page_normal);
            imgv.setTag(i);
            linBg.addView(imgv);
        }
        linBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HListView.this.getContext(),
                        "Don't touch " + v.getTag() + " , keep away from me !", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        addView(linBg);
        addView(linCur);
    }

    /**
     * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}. A
     * FlingRunnable will keep re-posting itself until the fling is done.
     */
    private class FlingRunnable implements Runnable {
        /**
         * How long the transition animation should run when a child view
         * changes position, measured in milliseconds.
         */
        private int mAnimationDuration = 400;

        /**
         * Tracks the decay of a fling scroll
         */
        private Scroller mScroller;

        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX;

        private int mItemCount = 1;

        private boolean mShouldStopFling;

        private Callback callback;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        public void startUsingDistance(int distance, Callback callback) {
            this.callback = callback;
            if (distance == 0)
                return;

            // Remove any pending flings
            removeCallbacks(this);

            // initial scroller
            mLastFlingX = 0;
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);

            // start work
            post(this);
        }

        public void stop() {
            removeCallbacks(this);
            endFling();
        }

        /**
         * End the fling and update the final position.
         */
        private void endFling() {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);
            /**
             * @TODO need to update the position.
             */
        }

        public void run() {

            if (mItemCount == 0) {
                endFling();
                return;
            }

            mShouldStopFling = false;

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();

            int delta = mLastFlingX - x;
            if (delta != 0) {
                if (callback != null) {
                    linCur.scrollTo(linCur.getScrollX() + delta, linCur.getScrollY());
                } else {
                    linBg.scrollTo(linBg.getScrollX() + delta, linBg.getScrollY());
                }
            }
            // Log.d(TAG, "x:" + x + "  delta:" + delta);

            if (more && !mShouldStopFling) {
                mLastFlingX = x;
                post(this);
            } else {
                endFling();
                if (callback != null) {
                    callback.handleMessage(Message.obtain());
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float bl = ITEM_W * 6;// bar length
        final float il = ITEM_W;// item length
        /**
         * One screen items' number, the number is the count of current screen's
         * items, not include the items off-screen.
         */
        final int bg_s = 5;
        /**
         * backgroud move step length, that means if move to next or previous
         * item, background will move this length.
         */
        final float bg_ms = (bl - screenWidth) / bg_s;

        float curX = event.getX();
        int curSelect = (int) ((bg_off_x + curX) / il);

        /**
         * compute the new backgroud offset x
         */
        bg_off_x += (curSelect - select) * bg_ms;
        bg_off_x = bg_off_x > bl - screenWidth ? bl - screenWidth : bg_off_x;

        int cur_x = (int) (bg_off_x - il * select);
        // linBg.scrollTo((int) bg_off_x, (int) bg_off_y);
        // linCur.scrollTo(cur_x, cur_y);
        mCurFling.startUsingDistance(cur_x - linCur.getScrollX(), new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                mCurFling.startUsingDistance((int) (bg_off_x - linBg.getScrollX()), null);
                return false;
            }
        });
        Log.d(TAG, "dis:" + (cur_x - linCur.getScrollX()));

        // select changed.
        select = curSelect;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:// ignore
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.EXACTLY | screenWidth, MeasureSpec.EXACTLY | 54);
    }

}
