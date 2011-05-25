
package com.coleman.kingword.activity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;

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
