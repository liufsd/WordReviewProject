
package com.coleman.kingword.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.provider.KingWord.StarDictIndex;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.wordlist.WordListManager;
import com.coleman.util.AppSettings;

public class WelcomeActivity extends Activity {
    private static final String TAG = WelcomeActivity.class.getName();

    private Button startButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.welcome);
        setContentView(R.layout.welcome);
        startButton = (Button) findViewById(R.id.w_button1);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DictManager.getInstance().initLibrary(WelcomeActivity.this);
                startActivity(new Intent(WelcomeActivity.this, WordListActivity.class));
            }
        });
        // _DEL_REPEAT_WORDS();
        ifFirstInstalled();
    }

    private void ifFirstInstalled() {
        boolean firstStarted = AppSettings.getBoolean(this, AppSettings.FIRST_STARTED_KEY, true);
        if (firstStarted) {
            AppSettings.saveBoolean(this, AppSettings.FIRST_STARTED_KEY, false);
            EbbinghausReminder.setNotifactionAfterInstalled(this);
            AppSettings.saveInt(this, AppSettings.SPLIT_NUM_KEY, WordListManager.DEFAULT_SPLIT_NUM);
            int c[][] = ColorSetActivityAsDialog.MODE_COLOR;
            String k[][] = AppSettings.COLOR_MODE;
            for (int i = 0; i < c.length; i++) {
                for (int j = 0; j < c[i].length; j++) {
                    AppSettings.saveInt(this, k[i][j], c[i][j]);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.welcome_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about_dev:
                showAboutDev();
                break;
            case R.id.menu_set:
                showSettings();
                break;
            default:
                break;
        }
        return true;
    }

    private void showSettings() {
        Intent it = new Intent(this, SettingsActivity.class);
        startActivity(it);
    }

    private void showAboutDev() {
        new AlertDialog.Builder(this).setTitle(R.string.about_dev).setMessage(loadFeatureList())
                .setPositiveButton(R.string.ok, null).show();
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
