
package com.coleman.kingword;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.coleman.http.json.bean.VersionCheckReq;
import com.coleman.http.json.bean.VersionCheckResp;
import com.coleman.http.json.bussiness.WorkManager;
import com.coleman.http.json.connection.SLRequest;
import com.coleman.http.json.connection.SLResponse;
import com.coleman.kingword.dict.DictLoadService;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.kingword.wordlist.WordListAccessor;
import com.coleman.kingword.wordlist.WordListActivity;
import com.coleman.kingword.wordlist.WordListManager;
import com.coleman.tools.InfoGather;
import com.coleman.util.AppSettings;
import com.coleman.util.GeneralParser;
import com.coleman.util.Log;
import com.coleman.util.Log.LogType;

/**
 * @author coleman
 */
public class WelcomeActivity extends Activity implements Observer {
    private static final String TAG = "WelcomeActivity";

    private Button startButton;

    private TextView curTV, nextTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.welcome);
        setContentView(R.layout.welcome);
        startButton = (Button) findViewById(R.id.w_button1);
        curTV = (TextView) findViewById(R.id.textView1);
        nextTV = (TextView) findViewById(R.id.textView2);

        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, WordListActivity.class));
            }
        });

        // initial the environment
        init();
        // test versionUpgrade
        upgradeCheck();
    }

    private void upgradeCheck() {
        try {
            VersionCheckReq req = new VersionCheckReq();
            String path = "kingword/release/release_note.txt";
            InputStream is = getAssets().open(path);
            HashMap<String, String> map = GeneralParser.parseFile(is);
            req.setVersionCode(Integer.parseInt(map.get("versionCode")));
            req.setVersionType(map.get("versionType"));
            SLRequest<VersionCheckReq> slReq = new SLRequest<VersionCheckReq>(req);
            WorkManager.getInstance().versionUpgrade(this, slReq);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        boolean firstStarted = AppSettings.getBoolean(this, AppSettings.FIRST_STARTED_KEY, true);
        if (firstStarted) {

            // mark first start app
            AppSettings.saveBoolean(this, AppSettings.FIRST_STARTED_KEY, false);

            // init ebbinghaus notification
            EbbinghausReminder.setNotifactionAfterInstalled(this);

            // /////////////////////////////////////////////////////
            // don't send sms to author every week, send when upgrade instead
            // SmsInfoGather.setSmsGatherRepeatNotifaction(this);
            // /////////////////////////////////////////////////////

            // save first start app time
            AppSettings.saveLong(this, AppSettings.FIRST_STARTED_TIME_KEY,
                    System.currentTimeMillis());

            // set default log type to warn
            Log.init(this);

            // set default unit split number
            AppSettings.saveInt(this, AppSettings.SPLIT_NUM_KEY, WordListManager.DEFAULT_SPLIT_NUM);

            // set default color configuration
            int c[][] = ColorSetActivityAsDialog.MODE_COLOR;
            String k[][] = AppSettings.COLOR_MODE;
            for (int i = 0; i < c.length; i++) {
                for (int j = 0; j < c[i].length; j++) {
                    AppSettings.saveInt(this, k[i][j], c[i][j]);
                }
            }

            // ///////////////////////////////////////////////
            // check if there is backup to restore
            // WordInfoHelper.restoreWordInfoDB(this, false);
            // ///////////////////////////////////////////////d
        } else {
            AppSettings.saveInt(this, AppSettings.STARTED_TOTAL_TIMES_KEY,
                    AppSettings.getInt(this, AppSettings.STARTED_TOTAL_TIMES_KEY, 1) + 1);
            Log.setLogType(this, LogType.instanse(AppSettings.getInt(this,
                    AppSettings.LOG_TYPE_KEY, LogType.verbose.value())));
        }

        // start a service to load library.
        startService(new Intent(this, DictLoadService.class));

        // set last study restore
        boolean isSave = AppSettings.getBoolean(this, AppSettings.SAVE_CACHE_KEY, false);
        if (isSave) {
            new AlertDialog.Builder(this).setTitle(R.string.recovery_tip)
                    .setMessage(R.string.recovery_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent i = new Intent(WelcomeActivity.this, CoreActivity.class);
                            i.putExtra("type", WordListAccessor.RECOVERY_LIST);
                            startActivity(i);
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
            AppSettings.saveBoolean(this, AppSettings.SAVE_CACHE_KEY, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLevels();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.welcome_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_set:
                showSettings();
                break;
            case R.id.menu_level_info:
                showLevelInfo();
                break;
            case R.id.menu_about_dev:
                showAboutDev();
                break;
            default:
                break;
        }
        return true;
    }

    private void showLevelInfo() {
        int levelType = AppSettings.getInt(this, AppSettings.LEVEL_TYPE_KEY, 0);
        int[] levelNums = getResources().getIntArray(R.array.level_num);
        String[] levelNames = (levelType == 0 ? getResources()
                .getStringArray(R.array.military_rank) : (levelType == 1 ? getResources()
                .getStringArray(R.array.leaning_level) : getResources().getStringArray(
                R.array.xiuzhen_level)));
        StringBuilder sb = new StringBuilder();
        String level_info_item = getString(R.string.level_info_item);
        for (int i = 0; i < levelNames.length; i++) {
            sb.append(String.format(level_info_item, i, levelNames[i], levelNums[i]));
        }
        new AlertDialog.Builder(this).setTitle(R.string.level_info)
                .setMessage(sb.subSequence(0, sb.length() - 1))
                .setPositiveButton(R.string.ok, null).show();
    }

    private void showSettings() {
        Intent it = new Intent(this, SettingsActivity.class);
        startActivity(it);
    }

    private void showAboutDev() {
        View view = LayoutInflater.from(this).inflate(R.layout.about, null);
        TextView tv3 = (TextView) view.findViewById(R.id.textView3);
        TextView tv5 = (TextView) view.findViewById(R.id.textView5);
        String version = "1";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            Log.d(TAG, info.toString());
            version = info.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        tv3.setText(version);
        tv5.setText("zouyf_1984@hotmail.com");
        new AlertDialog.Builder(this).setTitle(R.string.about).setView(view)
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
                THistory._ID, THistory.WORD
        };
        Cursor c = getContentResolver().query(THistory.CONTENT_URI, pro, null, null, null);
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
                    getContentResolver().delete(THistory.CONTENT_URI,
                            THistory._ID + "=" + info2.id, null);
                    info2.del = true;
                }
            }
        }

    }

    private synchronized void initLevels() {
        int count = 0, index = 0;
        String curLev = getString(R.string.cur_leve);
        String nextLev = getString(R.string.next_level);
        int levelType = AppSettings.getInt(this, AppSettings.LEVEL_TYPE_KEY, 0);
        int[] levelNums = getResources().getIntArray(R.array.level_num);
        String[] levelNames = (levelType == 0 ? getResources()
                .getStringArray(R.array.military_rank) : (levelType == 1 ? getResources()
                .getStringArray(R.array.leaning_level) : getResources().getStringArray(
                R.array.xiuzhen_level)));
        Cursor c = getContentResolver().query(THistory.CONTENT_URI, new String[] {
            THistory._ID
        }, null, null, THistory._ID + " desc LIMIT 1");
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            count = (int) id;
            Log.d(TAG, "id:" + id);
        }
        if (c != null) {
            c.close();
            c = null;
        }
        for (int i = levelNums.length - 1; i >= 0; i--) {
            if (count >= levelNums[i]) {
                index = i;
                break;
            }
        }
        curLev = String.format(curLev, levelNames[index], count);
        // check if send a msg to author
        InfoGather.checkLevelUpgrade(this, index);
        index++;
        curTV.setText(curLev);
        if (index >= levelNames.length) {
            nextLev = getString(R.string.top_level);
        } else {
            nextLev = String.format(nextLev, levelNames[index], levelNums[index]);
        }
        nextTV.setText(nextLev);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (!(observable instanceof SLResponse<?>)) {
            return;
        }
        if (((SLResponse<?>) observable).getResponse() instanceof VersionCheckResp) {
            final VersionCheckResp bean = (VersionCheckResp) ((SLResponse<?>) observable)
                    .getResponse();
            if (data == null) {
                int rc = bean.getResultCode();
                if (rc == 0) {
                    if (bean.getNewVersionCode() != -1) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.upgrade_tip)
                                .setMessage(bean.getDescription())
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                UpgradeService.downloadVersion(
                                                        WelcomeActivity.this, bean);
                                            }
                                        }).setNegativeButton(R.string.cancel, null).show();
                    }
                } else {
                    Log.i(TAG, "===coleman-debug-bean.getDescription():" + bean.getDescription());
                }

            } else {
                Log.i(TAG, "===coleman-debug-bean.getDescription():" + bean.getDescription());
            }
        }

    }

}
