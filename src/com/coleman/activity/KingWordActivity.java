
package com.coleman.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.coleman.dict.stardict.DictParser;

public class KingWordActivity extends Activity {
    TextView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (TextView) findViewById(R.id.text);
        new DictParser(this, handler);
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    view.setText(msg.obj.toString() + view.getText().toString());
                    break;
                default:
                    break;
            }
        }
    };
}
