
package com.coleman.kingword.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.coleman.kingword.R;
import com.coleman.kingword.activity.CoreActivity;
import com.coleman.kingword.activity.SubWordListActivity.SubInfo;
import com.coleman.kingword.provider.KingWord.SubWordsList;

public class SlideTableSwitcher extends ViewSwitcher implements OnClickListener {

    private static final String TAG = SlideTableSwitcher.class.getName();

    Button[] btns = new Button[12];

    TextView[] tvs = new TextView[12];

    private void initViews(TableLayout layout) {
        btns[0] = (Button) layout.findViewById(R.id.button1);
        btns[1] = (Button) layout.findViewById(R.id.button2);
        btns[2] = (Button) layout.findViewById(R.id.button3);

        btns[3] = (Button) layout.findViewById(R.id.button4);
        btns[4] = (Button) layout.findViewById(R.id.button5);
        btns[5] = (Button) layout.findViewById(R.id.button6);

        btns[6] = (Button) layout.findViewById(R.id.button7);
        btns[7] = (Button) layout.findViewById(R.id.button8);
        btns[8] = (Button) layout.findViewById(R.id.button9);

        btns[9] = (Button) layout.findViewById(R.id.button10);
        btns[10] = (Button) layout.findViewById(R.id.button11);
        btns[11] = (Button) layout.findViewById(R.id.button12);

        tvs[0] = (TextView) layout.findViewById(R.id.textView1);
        tvs[1] = (TextView) layout.findViewById(R.id.textView2);
        tvs[2] = (TextView) layout.findViewById(R.id.textView3);

        tvs[3] = (TextView) layout.findViewById(R.id.textView4);
        tvs[4] = (TextView) layout.findViewById(R.id.textView5);
        tvs[5] = (TextView) layout.findViewById(R.id.textView6);

        tvs[6] = (TextView) layout.findViewById(R.id.textView7);
        tvs[7] = (TextView) layout.findViewById(R.id.textView8);
        tvs[8] = (TextView) layout.findViewById(R.id.textView9);

        tvs[9] = (TextView) layout.findViewById(R.id.textView10);
        tvs[10] = (TextView) layout.findViewById(R.id.textView11);
        tvs[11] = (TextView) layout.findViewById(R.id.textView12);
        for (Button btn : btns) {
            btn.setOnClickListener(this);
        }
    }

    public SlideTableSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFactory(new SlideTableFactory(context));
        setVisibility(INVISIBLE);
        initViews((TableLayout) getCurrentView());
    }

    private int getTextId(int level) {
        if (level == 0) {
            return R.string.new_unit;
        } else if (level == 1) {
            return R.string.low_level;
        } else if (level == 2) {
            return R.string.mid_level;
        } else if (level == 3) {
            return R.string.high_level;
        }
        return R.string.new_unit;
    }

    public void setData(SubInfo sub_ids[]) {
        int i = 0;
        for (Button btn : btns) {
            if (i < sub_ids.length) {
                btn.setText("" + sub_ids[i].index);
                btn.setTag(sub_ids[i].id);
                btn.setVisibility(View.VISIBLE);
                tvs[i].setText(getTextId(sub_ids[i].level));
            } else {
                btn.setVisibility(View.GONE);
            }
            i++;
        }
    }

    public void showNextScreen(SubInfo sub_ids[]) {
        setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_right_in));
        setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_out));
        TableLayout l = (TableLayout) getNextView();
        l.setVisibility(VISIBLE);
        initViews(l);
        setData(sub_ids);
        showNext();
    }

    public void showPreviousScreen(SubInfo sub_ids[]) {
        setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_in));
        setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_right_out));
        TableLayout l = (TableLayout) getNextView();
        l.setVisibility(VISIBLE);
        initViews(l);
        setData(sub_ids);
        showPrevious();
    }

    public void showCurrentScreen(SubInfo sub_ids[]) {
        setVisibility(VISIBLE);
        setData(sub_ids);
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
                Intent intent = new Intent(getContext(), CoreActivity.class);
                long id = (Long) (v.getTag());
                intent.putExtra(SubWordsList._ID, id);
                getContext().startActivity(intent);
                break;
            default:
                break;
        }
    }

    public class SlideTableFactory implements ViewFactory {
        LayoutInflater mInflater;

        public SlideTableFactory(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View makeView() {
            return mInflater.inflate(R.layout.swl_table, null);
        }
    }

}
