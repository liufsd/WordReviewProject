
package com.coleman.kingword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HListView extends FrameLayout {
    LinearLayout linBg;

    LinearLayout linCur;

    ImageView cursorView;

    int screenWidth;

    float x;

    float y;

    int select;

    int[] menus = new int[] {
            1, 2, 3, 4, 5, 6
    };

    int[] menusSeleted = new int[] {
            1, 2, 3, 4, 5, 6
    };

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
                super.onMeasure(MeasureSpec.EXACTLY | 60, MeasureSpec.EXACTLY | 51);
            }
        };
        cursorView.setImageDrawable(curDrawable);
        linBg = new LinearLayout(getContext());
        linBg.setOrientation(LinearLayout.HORIZONTAL);
        linCur = new LinearLayout(getContext());
        linCur.setOrientation(LinearLayout.HORIZONTAL);
        linCur.addView(cursorView);
        for (int i = 0; i < menus.length; i++) {
            final ImageView imgv = new ImageView(getContext()) {
                @Override
                public void onMeasure(int width, int height) {
                    super.onMeasure(MeasureSpec.EXACTLY | 60, MeasureSpec.EXACTLY | 51);
                }
            };
            imgv.setImageDrawable(itemDrawable);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float bl = 60 * 6;// bar length
        final float il = 60;// item length
        final float ms = (bl - 320) / 5;// mini step length
        float curX = event.getX();
        int curSelect = (int) ((x + curX) / il);
        x += (curSelect - select) * ms;
        x = x > bl - 320 ? bl - 320 : x;
        select = curSelect;
        linBg.scrollTo((int) x, (int) y);
        linCur.scrollTo((int) (x - il * select), (int) y);
        cursorView.setImageDrawable(curDrawable);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // fall through
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                // ignore
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.EXACTLY | screenWidth, MeasureSpec.EXACTLY | 54);
    }

    private Drawable curDrawable = new Drawable() {
        Paint p = new Paint();

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public int getOpacity() {
            return 0;
        }

        @Override
        public void draw(Canvas canvas) {
            p.setColor(0xffffff00);
            canvas.drawRect(getBounds(), p);
        }
    };

    private Drawable itemDrawable = new Drawable() {
        Paint p = new Paint();

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public int getOpacity() {
            return 0;
        }

        @Override
        public void draw(Canvas canvas) {
            p.setColor(0xff00ff00);
            canvas.drawRect(getBounds(), p);
        }
    };
}
