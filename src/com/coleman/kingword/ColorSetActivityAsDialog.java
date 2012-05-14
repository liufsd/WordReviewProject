
package com.coleman.kingword;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.coleman.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.util.AppSettings;

public class ColorSetActivityAsDialog extends Activity implements OnClickListener,
        OnCheckedChangeListener, OnSeekBarChangeListener {
    private static final String TAG = "ColorSetActivityAsDialog";

    private TextView textFont;

    private RadioGroup sceneMode, fontOrBgMode;

    private TextView textRed, textGreen, textBlue, textTrans;

    private SeekBar seekRed, seekGreen, seekBlue, seekTrans;

    private Button btnOk, btnCancel, btnReset;

    private int selectMode;

    public static final int MODE_COLOR[][] = new int[][] {
            {
                    0xff000000, 0xfffff8dc, 0xffaaffff
            }, {
                    0xff6d96c9, 0xff000000, 0xff343434
            }, {
                    Color.BLACK, Color.WHITE, Color.GRAY
            }
    };

    private int textColor[] = new int[3], bgColor[] = new int[3], selectColor[] = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color_set_asdialog);
        initView();

        selectMode = AppSettings.getInt(this, AppSettings.SELECT_COLOR_MODE_KEY, 0);
        for (int i = 0; i < textColor.length; i++) {
            textColor[i] = AppSettings.getInt(this, AppSettings.COLOR_MODE[i][0], MODE_COLOR[i][0]);
            bgColor[i] = AppSettings.getInt(this, AppSettings.COLOR_MODE[i][1], MODE_COLOR[i][1]);
            selectColor[i] = AppSettings.getInt(this, AppSettings.COLOR_MODE[i][2],
                    MODE_COLOR[i][2]);
        }

        sceneMode.check(getIdbyMode(selectMode));
        textFont.setTextColor(textColor[selectMode]);
        textFont.setBackgroundColor(bgColor[selectMode]);
    }

    private int getIdbyMode(int mode) {
        int id;
        switch (mode) {
            case 1:
                id = R.id.radio1;
                break;
            case 2:
                id = R.id.radio2;
                break;
            case 0:
            default:
                id = R.id.radio0;
                break;
        }
        return id;
    }

    private void initView() {
        textFont = (TextView) findViewById(R.id.textView1);

        sceneMode = (RadioGroup) findViewById(R.id.radioGroup1);
        fontOrBgMode = (RadioGroup) findViewById(R.id.radioGroup2);

        textRed = (TextView) findViewById(R.id.textView2);
        textGreen = (TextView) findViewById(R.id.textView3);
        textBlue = (TextView) findViewById(R.id.textView4);
        textTrans = (TextView) findViewById(R.id.textView5);

        seekRed = (SeekBar) findViewById(R.id.seekBar1);
        seekRed.setMax(255);
        seekRed.setThumbOffset(seekRed.getThumbOffset() / 2);
        seekGreen = (SeekBar) findViewById(R.id.seekBar2);
        seekGreen.setMax(255);
        seekGreen.setThumbOffset(seekGreen.getThumbOffset() / 2);
        seekBlue = (SeekBar) findViewById(R.id.seekBar3);
        seekBlue.setMax(255);
        seekBlue.setThumbOffset(seekBlue.getThumbOffset() / 2);
        seekTrans = (SeekBar) findViewById(R.id.seekBar4);
        seekTrans.setMax(255);
        seekTrans.setThumbOffset(seekTrans.getThumbOffset() / 2);

        btnReset = (Button) findViewById(R.id.button1);
        btnOk = (Button) findViewById(R.id.positive);
        btnCancel = (Button) findViewById(R.id.negative);

        sceneMode.setOnCheckedChangeListener(this);
        fontOrBgMode.setOnCheckedChangeListener(this);

        seekRed.setOnSeekBarChangeListener(this);
        seekGreen.setOnSeekBarChangeListener(this);
        seekBlue.setOnSeekBarChangeListener(this);
        seekTrans.setOnSeekBarChangeListener(this);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positive:
                exit();
                break;
            case R.id.negative:
                finish();
                break;
            case R.id.button1:
                reset();
                break;
            default:// ignore
                break;
        }
    }

    private void reset() {
        for (int i = 0; i < MODE_COLOR.length; i++) {
            textColor[i] = MODE_COLOR[i][0];
            bgColor[i] = MODE_COLOR[i][1];
            selectColor[i] = MODE_COLOR[i][2];
        }
        setSeekProgress();
        textFont.setTextColor(textColor[selectMode]);
        textFont.setBackgroundColor(bgColor[selectMode]);
    }

    private void exit() {
        AppSettings.saveInt(this, AppSettings.SELECT_COLOR_MODE_KEY, selectMode);
        for (int i = 0; i < textColor.length; i++) {
            AppSettings.saveInt(this, AppSettings.COLOR_MODE[i][0], textColor[i]);
            AppSettings.saveInt(this, AppSettings.COLOR_MODE[i][1], bgColor[i]);
            AppSettings.saveInt(this, AppSettings.COLOR_MODE[i][2], selectColor[i]);
        }
        Intent it = new Intent(this, CoreActivity.class);
        it.putExtra(AppSettings.SELECT_COLOR_MODE_KEY, selectMode);
        setResult(RESULT_OK, it);
        finish();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBar1:
                textRed.setText(getString(R.string.sred) + "(" + progress + ")");
                break;
            case R.id.seekBar2:
                textGreen.setText(getString(R.string.sgreen) + "(" + progress + ")");
                break;
            case R.id.seekBar3:
                textBlue.setText(getString(R.string.sblue) + "(" + progress + ")");
                break;
            case R.id.seekBar4:
                textTrans.setText(getString(R.string.strans) + "(" + progress * 100 / 255 + "%)");
                break;
            default:
                break;
        }
        setColor();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void setColor() {
        int color = ((255 - seekTrans.getProgress()) << 24) + (seekRed.getProgress() << 16)
                + (seekGreen.getProgress() << 8) + seekBlue.getProgress();
        Log.d(TAG, "color: " + Integer.toHexString(color));
        if (((RadioButton) fontOrBgMode.findViewById(R.id.radio3)).isChecked()) {
            textColor[selectMode] = color;
            textFont.setTextColor(color);
        } else if (((RadioButton) fontOrBgMode.findViewById(R.id.radio4)).isChecked()) {
            bgColor[selectMode] = color;
            textFont.setBackgroundColor(color);
        } else {
            selectColor[selectMode] = color;
            textFont.setBackgroundColor(color);
        }
    }

    private void setSeekProgress() {
        int color = ((RadioButton) fontOrBgMode.findViewById(R.id.radio3)).isChecked() ? textColor[selectMode]
                : (((RadioButton) fontOrBgMode.findViewById(R.id.radio4)).isChecked() ? bgColor[selectMode]
                        : selectColor[selectMode]);
        int trans = 255 - ((color >>> 24) & 0xff);
        int red = (color >>> 16) & 0xff;
        int green = (color >>> 8) & 0xff;
        int blue = color & 0xff;
        seekTrans.setProgress(trans);
        seekRed.setProgress(red);
        seekGreen.setProgress(green);
        seekBlue.setProgress(blue);
        textTrans.setText(getString(R.string.strans) + "(" + trans * 100 / 255 + "%)");
        textRed.setText(getString(R.string.sred) + "(" + red + ")");
        textGreen.setText(getString(R.string.sgreen) + "(" + green + ")");
        textBlue.setText(getString(R.string.sblue) + "(" + blue + ")");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.radioGroup1:
                switch (checkedId) {
                    case R.id.radio0:
                        setSelectMode(0);
                        break;
                    case R.id.radio1:
                        setSelectMode(1);
                        break;
                    case R.id.radio2:
                        setSelectMode(2);
                        break;
                    default:
                        break;
                }
                break;
            case R.id.radioGroup2:
                switch (checkedId) {
                    case R.id.radio3:
                        setSeekProgress();
                        break;
                    case R.id.radio4:
                        setSeekProgress();
                        break;
                    case R.id.radio5:
                        setSeekProgress();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void setSelectMode(int mode) {
        selectMode = mode;
        textFont.setTextColor(textColor[selectMode]);
        textFont.setBackgroundColor(bgColor[selectMode]);
        setSeekProgress();
    }
}
