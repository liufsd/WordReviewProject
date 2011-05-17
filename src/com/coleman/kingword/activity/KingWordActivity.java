
package com.coleman.kingword.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.wordlist.WordListManager;

public class KingWordActivity extends Activity {
    private static final String TAG = KingWordActivity.class.getName();

    TextView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (TextView) findViewById(R.id.text);
        DictManager.getInstance().initLibrary(this);
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
