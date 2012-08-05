
package com.coleman.kingword;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.coleman.kingword.dict.DictLoadService;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.kingword.provider.upgrade.UpgradeManager;
import com.coleman.kingword.skin.ColorManager;
import com.coleman.kingword.wordlist.WordlistTabActivity;
import com.coleman.log.Log;
import com.coleman.log.Log.Level;
import com.coleman.ojm.bean.LoginReq;
import com.coleman.ojm.bean.LoginResp;
import com.coleman.ojm.bean.VersionCheckReq;
import com.coleman.ojm.bean.VersionCheckResp;
import com.coleman.ojm.bussiness.WorkManager;
import com.coleman.ojm.core.Observer;
import com.coleman.ojm.http.SLRequest;
import com.coleman.ojm.http.SLResponse;
import com.coleman.tools.InfoGather;
import com.coleman.tools.chart.ChartManager;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;
import com.coleman.util.DialogUtil;
import com.coleman.util.MyApp;

/**
 * @author coleman
 */
public class WelcomeActivity extends Activity implements Observer {
    private static final String TAG = WelcomeActivity.class.getName();

    private static Log Log = Config.getLog();

    private Button startButton;

    private TextView curTV, nextTV;

    private boolean userCheck;

    private Dialog versionCheckDialog;

    private AlertDialog dbUpgradeDialog;

    private FrameLayout flayout;

    private SLResponse<VersionCheckResp> slVersionCheckResp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.welcome);
        setContentView(R.layout.welcome);
        startButton = (Button) findViewById(R.id.w_button1);
        curTV = (TextView) findViewById(R.id.textView1);
        nextTV = (TextView) findViewById(R.id.textView2);
        flayout = (FrameLayout) findViewById(R.id.ebbinghaus);

        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, WordlistTabActivity.class));
            }
        });
        // check db upgrade
        if (UpgradeManager.getInstance().needUpgrade()) {
            UpgradeManager.getInstance().addObserver(this);
            UpgradeManager.getInstance().upgrade();
            dbUpgradeDialog = new ProgressDialog(this);
            dbUpgradeDialog.setCancelable(false);
            dbUpgradeDialog.setMessage(getString(R.string.db_upgrade));
            dbUpgradeDialog.show();
        } else {
            setup();
        }

    }

    private void setColorMode() {
        ColorManager mgr = ColorManager.getInstance();
        findViewById(R.id.relativeLayout1).setBackgroundColor(mgr.getBgColor());
        if (mgr.getSelectMode() == 1) {
            startButton.setBackgroundResource(R.drawable.btn_bg_night);
            startButton.setTextColor(mgr.getTextColor());
        } else {
            startButton.setBackgroundResource(android.R.drawable.btn_default);
            startButton.setTextColor(mgr.getTextColor());
        }
    }

    private void setup() {
        // initial the environment
        init();
        // login
        login();
        // test versionUpgrade
        if (AppSettings.getBoolean(AppSettings.VERSION_CHECK, false)) {
            upgradeCheck();
        }
    }

    private void login() {
        LoginReq req = InfoGather.gatherLoginInfo(this);
        req.setIMEI(Config.getDeviceId(this));
        req.setUserName(Config.getDeviceId(this));
        req.setPassword("");
        SLRequest<LoginReq> slReq = new SLRequest<LoginReq>(req);
        slReq.addObserver(this);
        WorkManager.getInstance().login(slReq);
    }

    private void upgradeCheck() {
        try {
            VersionCheckReq req = new VersionCheckReq();
            String appVersionName = null;
            int appVersionCode;
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            appVersionName = info.versionName; // 版本名
            appVersionCode = info.versionCode;
            req.setVersionCode(appVersionCode);
            req.setVersionType(appVersionName);
            SLRequest<VersionCheckReq> slReq = new SLRequest<VersionCheckReq>(req);
            slReq.addObserver(this);
            slVersionCheckResp = WorkManager.getInstance().versionUpgrade(slReq);
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    private void init() {

        // set default log type to warn
        if (Config.isSimulator(MyApp.context)
                || "A0000022C343AF".equals(Config.getDeviceId(MyApp.context))) {// A0000022C343AF
            Log.setPrintable(true);
            Log.setLevel(Level.verbose);
        } else {
            Log.setPrintable(false);
            Log.setLevel(Level.warning);
        }

        boolean firstStarted = AppSettings.getBoolean(AppSettings.FIRST_STARTED_KEY, true);
        if (firstStarted) {

            // mark first start app
            AppSettings.saveBoolean(AppSettings.FIRST_STARTED_KEY, false);

            // init ebbinghaus notification
            EbbinghausReminder.setNotifactionAfterInstalled(this);

            // /////////////////////////////////////////////////////
            // don't send sms to author every week, send when upgrade instead
            // SmsInfoGather.setSmsGatherRepeatNotifaction(this);
            // /////////////////////////////////////////////////////

            // save first start app time
            AppSettings.saveLong(AppSettings.FIRST_STARTED_TIME_KEY, System.currentTimeMillis());

            // set default color configuration
            int c[][] = ColorSetActivityAsDialog.MODE_COLOR;
            String k[][] = AppSettings.COLOR_MODE;
            for (int i = 0; i < c.length; i++) {
                for (int j = 0; j < c[i].length; j++) {
                    AppSettings.saveInt(k[i][j], c[i][j]);
                }
            }

            // ///////////////////////////////////////////////
            // check if there is backup to restore
            // WordInfoHelper.restoreWordInfoDB(this, false);
            // ///////////////////////////////////////////////d
        } else {
            // 重设闹钟
            EbbinghausReminder.resetRepeatNotification();
            // 本地登陆计数
            AppSettings.saveInt(AppSettings.STARTED_TOTAL_TIMES_KEY,
                    AppSettings.getInt(AppSettings.STARTED_TOTAL_TIMES_KEY, 1) + 1);
            Log.setLevel(Level.getLevel(AppSettings.getInt(AppSettings.LOG_TYPE_KEY,
                    Level.verbose.value)));
        }

        // start a service to load library.
        startService(new Intent(this, DictLoadService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        flayout.removeAllViews();
        flayout.addView(ChartManager.getInstance().getChartView(this,
                ColorManager.getInstance().getBgColor(), ColorManager.getInstance().getTextColor()));
        setColorMode();
        initLevels();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (slVersionCheckResp != null) {
            slVersionCheckResp.deleteObserver(this);
        }
        super.onDestroy();
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
            case R.id.menu_version:
                userCheck();
                break;
            default:
                break;
        }
        return true;
    }

    private void userCheck() {
        upgradeCheck();
        versionCheckDialog = DialogUtil.showLoadingDialog(this, R.string.upgrade_check);
        userCheck = true;
    }

    private void showLevelInfo() {
        String entries[] = getResources().getStringArray(R.array.level_type);
        String levelType = AppSettings.getString(AppSettings.LEVEL, entries[0]);
        int[] levelNums = getResources().getIntArray(R.array.level_num);
        String[] levelNames = (levelType.equals(entries[0]) ? getResources().getStringArray(
                R.array.military_rank) : (levelType.equals(entries[1]) ? getResources()
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
        Intent it = new Intent(this, Settings.class);
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
        String entries[] = getResources().getStringArray(R.array.level_type);
        String levelType = AppSettings.getString(AppSettings.LEVEL, entries[0]);
        int[] levelNums = getResources().getIntArray(R.array.level_num);
        String[] levelNames = (levelType.equals(entries[0]) ? getResources().getStringArray(
                R.array.military_rank) : (levelType.equals(entries[1]) ? getResources()
                .getStringArray(R.array.leaning_level) : getResources().getStringArray(
                R.array.xiuzhen_level)));
        Cursor c = getContentResolver().query(THistory.CONTENT_URI, new String[] {
            "count(*) as count"
        }, null, null, null);
        if (c.moveToFirst()) {
            count = c.getInt(0);
            Log.i(TAG, "===coleman-debug-count:" + count);
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
        // ******************************************
        // check if send a msg to author
        // this method is deprecated
        // InfoGather.checkLevelUpgrade(this, index);
        // ******************************************
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
    public void update(Object data) {
        if (data instanceof VersionCheckResp) {
            final VersionCheckResp bean = (VersionCheckResp) data;
            int rc = bean.getResultCode();
            dismiss(versionCheckDialog);
            if (rc == 0) {
                if (bean.getNewVersionCode() != -1) {
                    new AlertDialog.Builder(this).setTitle(R.string.upgrade_tip)
                            .setMessage(bean.getDescription())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UpgradeService.downloadVersion(WelcomeActivity.this, bean);
                                }
                            }).setNegativeButton(R.string.cancel, null).show();
                } else {
                    if (userCheck) {
                        userCheck = false;
                        new AlertDialog.Builder(this).setTitle(R.string.system_msg_title)
                                .setMessage(bean.getDescription())
                                .setPositiveButton(android.R.string.ok, null).show();
                    } else {
                        Log.i(TAG,
                                "===coleman-debug-bean.getDescription(): " + bean.getDescription());
                    }
                }

            } else {
                if (userCheck) {
                    userCheck = false;
                    new AlertDialog.Builder(this).setTitle(R.string.error_msg_title)
                            .setMessage(bean.getDescription())
                            .setPositiveButton(android.R.string.ok, null).show();
                } else {
                    Log.i(TAG, "===coleman-debug-bean.getDescription(): " + data == null ? ""
                            : data.toString());
                }
            }
        } else if (data instanceof LoginResp) {
            final LoginResp bean = (LoginResp) data;
            int rc = bean.getResultCode();
            String desc = bean.getDescription();
            Log.i(TAG, "===coleman-debug-rc: " + rc + " desc:" + desc);
        } else if (data instanceof UpgradeManager) {
            if (!TextUtils.isEmpty(UpgradeManager.getInstance().getFailMsg())) {
                new AlertDialog.Builder(this).setTitle(R.string.system_msg_title)
                        .setMessage(UpgradeManager.getInstance().getFailMsg())
                        .setPositiveButton(android.R.string.ok, null).show();
                UpgradeManager.getInstance().setFailMsg("");
            }
            dismiss(dbUpgradeDialog);
            setup();
        }
    }

    private void dismiss(Dialog versionCheckDialog) {
        if (versionCheckDialog != null && versionCheckDialog.isShowing()) {
            versionCheckDialog.dismiss();
        }
    }

}
