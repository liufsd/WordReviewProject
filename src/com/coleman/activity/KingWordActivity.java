
package com.coleman.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.coleman.dict.DictManager;
import com.coleman.wordlist.WordManager;

public class KingWordActivity extends Activity {
    private static final String TAG = KingWordActivity.class.getName();

    TextView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (TextView) findViewById(R.id.text);
        try {
            WordManager.getInstance().addWordList(this,
                    "kingword/wordlist/postgraduate/postgraduate.wl", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> list = WordManager.getInstance().getWordList(
                "kingword/wordlist/postgraduate/postgraduate.wl");
        DictManager.getInstance().initLibrary(this);
        for (String string : list) {
            Log.e(TAG, string + ":");
            String data = DictManager.getInstance().viewWord(this, string);
            Log.d(TAG, data);
        }
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
