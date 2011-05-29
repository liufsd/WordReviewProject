
package com.coleman.kingword.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.provider.KingWord.WordInfo;

public class WelcomeActivity extends Activity {
    private static final String TAG = WelcomeActivity.class.getName();

    private Button startButton;

    private TextView featureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        startButton = (Button) findViewById(R.id.w_button1);
        featureView = (TextView) findViewById(R.id.feature);
        featureView.setText(loadFeatureList());
        // applyAnim();
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DictManager.getInstance().initLibrary(WelcomeActivity.this);
                startActivity(new Intent(WelcomeActivity.this, WordListActivity.class));
            }
        });
        // _DEL_REPEAT_WORDS();
    }

    /**
     * for debug & test.
     * 
     * @deprecated
     */
    void _DEL_REPEAT_WORDS() {
        class Info {
            Info(long id, String word) {
                this.id = id;
                this.word = word;
            }

            long id;

            String word;

            boolean del = false;

            @Override
            public String toString() {
                return id + ": " + word;
            }
        }
        String pro[] = new String[] {
                WordInfo._ID, WordInfo.WORD
        };
        Cursor c = getContentResolver().query(WordInfo.CONTENT_URI, pro, null, null, null);
        ArrayList<Info> list = new ArrayList<Info>();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Info info = new Info(c.getLong(0), c.getString(1));
                list.add(info);
                c.moveToNext();
            }
        }
        if (c != null) {
            c.close();
        }
        for (Info info : list) {
            for (Info info2 : list) {
                if (!info.del && info2.id != info.id && info2.word.equals(info.word) && !info2.del) {
                    Log.d(TAG, info + " >>> " + info2);
                    getContentResolver().delete(WordInfo.CONTENT_URI,
                            WordInfo._ID + "=" + info2.id, null);
                    info2.del = true;
                }
            }
        }

    }

    private void applyAnim() {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        a.setDuration(30000);
        a.setRepeatMode(Animation.RESTART);
        a.setRepeatCount(Animation.INFINITE);
        featureView.startAnimation(a);
    }

    private String loadFeatureList() {
        long time = System.currentTimeMillis();
        String str = "no feature found!";
        try {
            InputStream is = getAssets().open("kingword/featurelist");
            int v;
            byte bytes[] = new byte[1024];
            ByteArrayBuffer baf = new ByteArrayBuffer(1024 * 200);
            while ((v = is.read(bytes)) != -1) {
                baf.append(bytes, 0, v);
            }
            is.close();
            is = null;
            str = new String(baf.toByteArray());
            baf.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        Log.d(TAG, "load feature list cost time: " + time);
        return str;
    }
}
