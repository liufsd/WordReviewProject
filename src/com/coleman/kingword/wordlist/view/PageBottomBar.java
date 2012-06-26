
package com.coleman.kingword.wordlist.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.coleman.kingword.R;
import com.coleman.log.Log;
import com.coleman.util.Config;

public class PageBottomBar extends ViewGroup {

    private static final String TAG = PageBottomBar.class.getName();

    private int mCurScreen = 0;

    private Log Log = Config.getLog();

    public PageBottomBar(Context context) {
        super(context);
    }

    public PageBottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageBottomBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        if (changed && childCount > 0) {
            int pl = getLeft();
            int pt = getTop();
            int pr = getRight();
            int pb = getBottom();
            Log.i(TAG, "===coleman-debug-pl: " + pl + " pt:" + pt + " pr:" + pr + " pb:" + pb);
            int dx = (pr - pl) / childCount;
            int cl = pl;
            for (int i = 0; i < childCount; i++) {
                final View childView = getChildAt(i);
                if (childView.getVisibility() != View.GONE) {
                    final int childWidth = childView.getMeasuredWidth();
                    Log.d(TAG, "childLeft:" + cl + "  childWidth:" + childWidth);
                    childView.layout(cl, pt, cl + dx, pt + childView.getMeasuredHeight());
                    cl += dx;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        @SuppressWarnings("unused")
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(width, width);
        }
    }

    public void setCurScreenIndex(int idx) {
        this.mCurScreen = idx;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (mCurScreen == i) {
                ((ImageView) childView).setImageResource(R.drawable.guide_dot_white);
            } else {
                ((ImageView) childView).setImageResource(R.drawable.guide_dot_black);
            }
        }
        invalidate();
    }
}
