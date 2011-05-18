
package com.coleman.kingword.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.wordlist.WordList.InternalWordList;
import com.coleman.kingword.wordlist.WordListManager;

public class KingWordActivity extends Activity {
    private static final String TAG = KingWordActivity.class.getName();

    private ArrayList<DictData> list = new ArrayList<DictData>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kingword);
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DictManager.getInstance().initLibrary(KingWordActivity.this);
                WordListManager wm = WordListManager.getInstance();
                if (wm.isExist(KingWordActivity.this, InternalWordList.POSTGRADUATE_WORDLIST)) {
                    wm.loadWordList(KingWordActivity.this, 1,
                            InternalWordList.POSTGRADUATE_WORDLIST);
                } else {
                    wm.loadWordList(KingWordActivity.this, InternalWordList.POSTGRADUATE_WORDLIST,
                            true);
                }
                startActivity(new Intent(KingWordActivity.this, ParaphraseActivity.class));
            }
        });
    }
}
