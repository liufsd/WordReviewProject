
package com.coleman.kingword.study;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.info.InfoGather;
import com.coleman.kingword.info.email.GMailSenderHelper;
import com.coleman.kingword.study.review.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.study.unit.model.FiniteStateMachine.InitState;
import com.coleman.kingword.study.unit.model.FiniteStateMachine.MultipleState;
import com.coleman.kingword.study.unit.model.SliceWordList;
import com.coleman.kingword.study.wordinfo.WordInfoHelper;
import com.coleman.kingword.study.wordlist.model.WordListManager;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;
import com.coleman.util.Log;
import com.coleman.util.Log.LogType;

public class SettingsActivity extends Activity implements OnItemClickListener {
    protected static final String TAG = SettingsActivity.class.getName();

    private ListView listView;

    private SettingsAdapter adapter;

    private ArrayList<HashMap<String, Integer>> data = new ArrayList<HashMap<String, Integer>>();

    private static final String from[] = new String[] {
            "item_icon", "item_name"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        prepareData();

        adapter = new SettingsAdapter(this);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private void prepareData() {
        HashMap<String, Integer> map;
        // 0
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_split_num);
        map.put(from[1], R.string.set_split_num_hint);
        data.add(map);
        // 1
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_review_time);
        map.put(from[1], R.string.set_review_time);
        data.add(map);
        // // 2
        // map = new HashMap<String, Integer>();
        // map.put(from[0], R.drawable.set_backup);
        // map.put(from[1], R.string.backup);
        // data.add(map);
        // // 3
        // map = new HashMap<String, Integer>();
        // map.put(from[0], R.drawable.set_restore);
        // map.put(from[1], R.string.restore);
        // data.add(map);
        // 2
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_level_type);
        map.put(from[1], R.string.learning_level_name_set);
        data.add(map);
        // 3
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_security);
        map.put(from[1], R.string.security_set);
        data.add(map);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                showSelectSplitNum();
                break;
            case 1:
                showReviewTimeList();
                break;
            case 2:
                showSelectLevelType();
                break;
            case 3:
                showSecurity();
                break;
            default:
                break;
        }
    }

    private void showGetPwRequest() {
        View layout = LayoutInflater.from(this).inflate(R.layout.get_pw_fordialog, null);
        final EditText et = (EditText) layout.findViewById(R.id.editText1);
        DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        long time = AppSettings.getLong(SettingsActivity.this,
                                AppSettings.LAST_SEND_GET_PW_REQUEST_TIME_KEY, 0);
                        if (System.currentTimeMillis() - time > 3 * 24 * 3600 * 1000) {
                            String IEMI = Config.getDeviceId(SettingsActivity.this);
                            String msg = "IEMI:" + IEMI + "\n";
                            msg += et.getText().toString() + "\n";
                            msg += InfoGather.gatherDetail(SettingsActivity.this);
                            GMailSenderHelper.sendMail(getString(R.string.request_pw), msg);
                            AppSettings.saveLong(SettingsActivity.this,
                                    AppSettings.LAST_SEND_GET_PW_REQUEST_TIME_KEY,
                                    System.currentTimeMillis());
                            dialog(R.string.get_pw_request_sent);
                        } else {
                            dialog(R.string.get_pw_request_send_wait);
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.security_set).setView(layout)
                .setPositiveButton(R.string.ok, lis).setNegativeButton(R.string.cancel, lis).show();
    }

    private void showSecurity() {
        if (checkStoredPwMatched()) {
            showHighLevelSettings();
            return;
        }
        View layout = LayoutInflater.from(this).inflate(R.layout.security_set_fordialog, null);
        final EditText et = (EditText) layout.findViewById(R.id.editText1);
        CheckBox cb = (CheckBox) layout.findViewById(R.id.checkBox1);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        checkPwSent(et.getText().toString());
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        showGetPwRequest();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.security_set).setView(layout)
                .setPositiveButton(R.string.ok, lis).setNeutralButton(R.string.get_pw, lis)
                .setNegativeButton(R.string.cancel, lis).show();
    }

    private boolean checkStoredPwMatched() {
        String saved_pw = AppSettings
                .getString(SettingsActivity.this, AppSettings.SAVED_PW_KEY, "");
        String count_pw = getCalculatedPw();
        String superpw = getSuperPW();
        // Log.d(TAG, "==================saved pw:" + saved_pw + "  count pw:" +
        // count_pw);
        if (saved_pw.equals(count_pw) || saved_pw.equals(superpw)) {
            return true;
        }
        return false;
    }

    private String getSuperPW() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String superpw = "" + (c.get(Calendar.MONTH) + 1 + 10)
                + (c.get(Calendar.DAY_OF_MONTH) + 11);
        return superpw;
    }

    private void checkPwSent(String pw) {
        String count_pw = getCalculatedPw();
        String super_pw = getSuperPW();
        String super_pw2 = "";
        try {
            super_pw2 = SimpleCrypto.encrypt(SimpleCrypto.KEY, super_pw);
            // Log.d(TAG, "super pw 2 " + super_pw2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pw.equals(count_pw)) {
            // Log.d(TAG, "================password matched!");
            AppSettings.saveString(SettingsActivity.this, AppSettings.SAVED_PW_KEY, count_pw);
            showHighLevelSettings();

        } else if (pw.equals(super_pw) || pw.equals(super_pw2)) {
            // Log.d(TAG, "================super password matched!");
            AppSettings.saveString(SettingsActivity.this, AppSettings.SAVED_PW_KEY, super_pw);
            showHighLevelSettings();
        } else {

            toast(R.string.pw_failed);
            // Log.d(TAG, "================password not matched!");
        }
    }

    private String getCalculatedPw() {
        String IMEI = Config.getDeviceId(this);
        String pw = "";
        try {
            // Log.d(TAG, "==================IMEI is " + IMEI);
            pw = SimpleCrypto.encrypt(SimpleCrypto.KEY, IMEI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pw;
    }

    private void showHighLevelSettings() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showHLS_SelectDebugLevel();
                        break;
                    case 1:
                        showBackupDialog();
                        break;
                    case 2:
                        showRestoreDialog();
                        break;
                    case 3:
                        showViewMethodConfigDialog();
                        break;
                    case 4:
                        showLanguageDialog();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }

        };
        new AlertDialog.Builder(this).setTitle(R.string.high_level_settings)
                .setItems(R.array.high_level_settings, listener)
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void showLanguageDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        DictManager.getInstance().setCurLibrary(DictLibrary.STARDICT);
                        AppSettings.saveInt(SettingsActivity.this, AppSettings.LANGUAGE_TYPE, 0);
                        break;
                    case 1:
                        DictManager.getInstance().setCurLibrary(DictLibrary.BABYLON_ENG);
                        AppSettings.saveInt(SettingsActivity.this, AppSettings.LANGUAGE_TYPE, 1);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }

        };
        int index = AppSettings.getInt(this, AppSettings.LANGUAGE_TYPE, 0);
        new AlertDialog.Builder(this).setTitle(R.string.language_settings)
                .setSingleChoiceItems(R.array.language_settings, index, listener)
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void mapIntToStr(ArrayList<Integer> intlist, ArrayList<String> strlist) {
        strlist.clear();
        String[] str = getResources().getStringArray(R.array.view_methods);
        for (int i = 0; i < intlist.size(); i++) {
            int v = intlist.get(i);
            if (v == InitState.TYPE) {
                strlist.add(str[0]);
            } else if (v == MultipleState.TYPE) {
                strlist.add(str[1]);
            }
        }
    }

    private void mapStrToInt(ArrayList<String> strlist, ArrayList<Integer> intlist) {
        intlist.clear();
        String[] str = getResources().getStringArray(R.array.view_methods);
        for (int i = 0; i < strlist.size(); i++) {
            String v = strlist.get(i);
            if (v.equals(str[0])) {
                intlist.add(InitState.TYPE);
            } else if (v.equals(str[1])) {
                intlist.add(MultipleState.TYPE);
            }
        }
    }

    private void showViewMethodConfigDialog() {
        final View view = LayoutInflater.from(this).inflate(R.layout.view_method_fordialog, null);
        final RadioButton radioSub = (RadioButton) view.findViewById(R.id.radio0);
        final RadioButton radioReview = (RadioButton) view.findViewById(R.id.radio1);
        final RadioButton radioNewbook = (RadioButton) view.findViewById(R.id.radio2);
        final RadioButton radioIgnore = (RadioButton) view.findViewById(R.id.radio3);
        final ListView listview = (ListView) view.findViewById(R.id.listView1);
        final Spinner spinner1 = (Spinner) view.findViewById(R.id.spinner1);
        final Button addBtn = (Button) view.findViewById(R.id.button0);
        final Button clearBtn = (Button) view.findViewById(R.id.button1);

        final ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < 4; i++) {
            result.add(new ArrayList<Integer>());
        }

        class Index {
            int i;
        }
        final ArrayList<String> list = new ArrayList<String>();
        final ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this,
                R.layout.textview_item_white, list);
        final Index selectIndex = new Index();
        String viewMtd = AppSettings.getString(this, AppSettings.VIEW_METHOD_KEY,
                SliceWordList.DEFAULT_VIEW_METHOD);
        String scrap[] = viewMtd.split("#");
        for (int i = 0; i < scrap.length; i++) {
            String ss[] = scrap[i].split(",");
            for (int j = 0; j < ss.length; j++) {
                result.get(i).add(Integer.parseInt(ss[j]));
            }
        }

        OnClickListener radioLis = new OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                switch (buttonView.getId()) {
                    case R.id.radio0:
                        radioSub.setChecked(true);
                        radioReview.setChecked(false);
                        radioNewbook.setChecked(false);
                        radioIgnore.setChecked(false);
                        mapStrToInt(list, result.get(selectIndex.i));
                        selectIndex.i = 0;
                        mapIntToStr(result.get(selectIndex.i), list);
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    case R.id.radio1:
                        radioSub.setChecked(false);
                        radioReview.setChecked(true);
                        radioNewbook.setChecked(false);
                        radioIgnore.setChecked(false);
                        mapStrToInt(list, result.get(selectIndex.i));
                        selectIndex.i = 1;
                        mapIntToStr(result.get(selectIndex.i), list);
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    case R.id.radio2:
                        radioSub.setChecked(false);
                        radioReview.setChecked(false);
                        radioNewbook.setChecked(true);
                        radioIgnore.setChecked(false);
                        mapStrToInt(list, result.get(selectIndex.i));
                        selectIndex.i = 2;
                        mapIntToStr(result.get(selectIndex.i), list);
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    case R.id.radio3:
                        radioSub.setChecked(false);
                        radioReview.setChecked(false);
                        radioNewbook.setChecked(false);
                        radioIgnore.setChecked(true);
                        mapStrToInt(list, result.get(selectIndex.i));
                        selectIndex.i = 3;
                        mapIntToStr(result.get(selectIndex.i), list);
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
        radioSub.setOnClickListener(radioLis);
        radioReview.setOnClickListener(radioLis);
        radioNewbook.setOnClickListener(radioLis);
        radioIgnore.setOnClickListener(radioLis);

        mapIntToStr(result.get(selectIndex.i), list);
        listview.setAdapter(listViewAdapter);

        final String spinnerArr[] = getResources().getStringArray(R.array.view_methods);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.textview_item, spinnerArr);
        spinner1.setAdapter(spinnerAdapter);
        OnClickListener btnlis = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button0:
                        list.add(spinnerArr[spinner1.getSelectedItemPosition()]);
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    case R.id.button1:
                        list.clear();
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
        addBtn.setOnClickListener(btnlis);
        clearBtn.setOnClickListener(btnlis);

        DialogInterface.OnClickListener dialis = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mapStrToInt(list, result.get(selectIndex.i));
                        String rstr = "";
                        for (int i = 0; i < result.size(); i++) {
                            ArrayList<Integer> slist = result.get(i);
                            if (slist.size() == 0) {
                                Toast.makeText(SettingsActivity.this,
                                        R.string.view_method_empty_warn, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int j = 0; j < slist.size(); j++) {
                                if (j == 0) {
                                    rstr += slist.get(j);
                                } else {
                                    rstr += "," + slist.get(j);
                                }
                            }
                            if (i != result.size() - 1) {
                                rstr += "#";
                            }
                        }
                        Log.d(TAG, "===============set new view method: " + rstr);
                        AppSettings.saveString(SettingsActivity.this, AppSettings.VIEW_METHOD_KEY,
                                rstr);
                        Toast.makeText(SettingsActivity.this, R.string.view_method_success_hint,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.view_method_dialog_title).setView(view)
                .setPositiveButton(R.string.ok, dialis).setNegativeButton(R.string.cancel, dialis)
                .show();
    }

    private void showHLS_SelectDebugLevel() {
        int log_type = Log.getLogType().value();
        Log.d(TAG, "====================cur log type:" + log_type);
        final String debugLevs[] = getResources().getStringArray(R.array.debug_level);
        final int debugLevNums[] = getResources().getIntArray(R.array.debug_level_value);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.setLogType(SettingsActivity.this, LogType.instanse(debugLevNums[which]));
                Log.d(TAG, "===================save log type:" + debugLevs[which] + " value:"
                        + debugLevNums[which]);
                toast("debug level changed to " + debugLevs[which]);
                dialog.dismiss();
            }
        };
        int select = 0;
        for (int i = 0; i < debugLevNums.length; i++) {
            if (log_type == debugLevNums[i]) {
                select = i;
                break;
            }
        }
        Log.d(TAG, "======================select log type:" + select);
        new AlertDialog.Builder(this).setTitle(R.string.learning_level_name_set)
                .setSingleChoiceItems(debugLevs, select, listener).show();
    }

    private void showSelectLevelType() {
        final String[] nums = getResources().getStringArray(R.array.level_type);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppSettings.saveInt(SettingsActivity.this, AppSettings.LEVEL_TYPE_KEY, which);
                dialog.dismiss();
            }
        };
        int checkedIndex = AppSettings.getInt(SettingsActivity.this, AppSettings.LEVEL_TYPE_KEY, 0);
        new AlertDialog.Builder(this).setTitle(R.string.learning_level_name_set)
                .setSingleChoiceItems(nums, checkedIndex, listener).show();
    }

    private void showBackupDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        showBackupItems();
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.backup).setMessage(R.string.backup_msg)
                .setPositiveButton(R.string.ok, listener)
                .setNegativeButton(R.string.cancel, listener).show();
    }

    private void showBackupItems() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        backupDB(SettingsActivity.this, true);
                        break;
                    case 1:
                        WordInfoHelper.backupWordInfoDB(SettingsActivity.this, true);
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.backup).setItems(R.array.backup, listener)
                .show();
    }

    private void backupDB(final Context context, final boolean toast) {
        if (!Config.isExternalMediaMounted()) {
            if (toast) {
                Toast.makeText(context, "External media not mounted!", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("External media not mounted!");
            }
            return;
        }
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (toast) {
                            Toast.makeText(context.getApplicationContext(), "Backup successful!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("Backup successful!");
                        }
                        break;
                    case 1:
                        if (toast) {
                            Toast.makeText(context.getApplicationContext(),
                                    "Backup failed, please check if your device has root!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            System.out
                                    .println("Backup failed, please check if your device has root!");
                        }
                        break;
                    default:
                        // ignore
                        break;
                }
            }
        };
        new Thread() {
            public void run() {
                long time = System.currentTimeMillis();
                try {
                    Runtime.getRuntime()
                            .exec("cp /data/data/com.coleman.kingword/databases/kingword.db /sdcard/kingword");
                    Runtime.getRuntime()
                            .exec("cp /data/data/com.coleman.kingword/shared_prefs/settings.xml /sdcard/kingword");
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                }
                System.out.println("backup cost time: " + (time - System.currentTimeMillis()));
            }
        }.start();
    }

    private void showRestoreDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        showRestoreItems();
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.restore).setMessage(R.string.restore_msg)
                .setPositiveButton(R.string.ok, listener)
                .setNegativeButton(R.string.cancel, listener).show();
    }

    private void showRestoreItems() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        restoreDB(SettingsActivity.this, true);
                        break;
                    case 1:
                        WordInfoHelper.restoreWordInfoDB(SettingsActivity.this, true);
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.restore)
                .setItems(R.array.restore, listener).show();
    }

    public static void restoreDB(final Context context, final boolean toast) {
        if (!Config.isExternalMediaMounted()) {
            if (toast) {
                Toast.makeText(context, "External media not mounted!", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("External media not mounted!");
            }
            return;
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (toast) {
                            Toast.makeText(context.getApplicationContext(), "Restore successful!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("Restore successful!");
                        }
                        break;
                    case 1:
                        if (toast) {
                            Toast.makeText(context.getApplicationContext(),
                                    "Restore failed, try other ways!", Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("Restore failed, try other ways!");
                        }
                        break;
                    default:
                        // ignore
                        break;
                }
            }
        };
        new Thread() {
            public void run() {
                long time = System.currentTimeMillis();
                try {
                    Runtime.getRuntime()
                            .exec("cp /sdcard/kingword/kingword.db /data/data/com.coleman.kingword/databases/");
                    Runtime.getRuntime()
                            .exec("cp /sdcard/kingword/settings.xml /data/data/com.coleman.kingword/shared_prefs/");
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                }
                time = System.currentTimeMillis() - time;
                System.out.println("restore cost time: " + time);
            }
        }.start();
    }

    private void showReviewTimeList() {
        final String[] time = new String[AppSettings.REVIEW_TIME_KEY.length];
        for (int i = 0; i < time.length; i++) {
            String str = AppSettings.getString(this, AppSettings.REVIEW_TIME_KEY[i],
                    getString(R.string.not_set));
            time[i] = str;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        for (int i = 0; i < time.length; i++) {
                            AppSettings.saveString(SettingsActivity.this,
                                    AppSettings.REVIEW_TIME_KEY[i],
                                    SettingsActivity.this.getString(R.string.not_set));
                            EbbinghausReminder.removeRepeatNotifaction(SettingsActivity.this, i);
                        }
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        showSetReviewTime(which, time[which]);
                        Log.d(TAG, "set review time: " + time[which]);
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.set_review_time).setItems(time, listener)
                .setPositiveButton(R.string.clear, listener)
                .setNegativeButton(R.string.cancel, listener).show();
    }

    private void showSetReviewTime(final int which, String time) {
        OnTimeSetListener callBack = new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String _time = hourOfDay + ":" + minute;
                AppSettings.saveString(SettingsActivity.this, AppSettings.REVIEW_TIME_KEY[which],
                        _time);
                EbbinghausReminder.setRepeatNotifaction(SettingsActivity.this, which, _time);
                Log.d(TAG, "set new review time: " + _time);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (!time.equals(getString(R.string.not_set))) {
            int idx = time.indexOf(":");
            hourOfDay = Integer.parseInt(time.substring(0, idx));
            minute = Integer.parseInt(time.substring(idx + 1));
        }
        new TimePickerDialog(this, callBack, hourOfDay, minute, true).show();
    }

    private void showSelectSplitNum() {
        final String[] nums = getResources().getStringArray(R.array.split_num);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppSettings.saveInt(SettingsActivity.this, AppSettings.SPLIT_NUM_KEY,
                        Integer.parseInt(nums[which]));
                dialog.dismiss();
                Log.d(TAG, "set split nums: " + nums[which]);
            }
        };
        int selectNum = AppSettings.getInt(SettingsActivity.this, AppSettings.SPLIT_NUM_KEY,
                WordListManager.DEFAULT_SPLIT_NUM);
        int checkedIndex = getCheckedIndex(nums, selectNum);
        new AlertDialog.Builder(this).setTitle(R.string.set_split_num_hint)
                .setSingleChoiceItems(nums, checkedIndex, listener).show();
    }

    private int getCheckedIndex(String[] nums, int selectNum) {
        int i = 0;
        for (String num : nums) {
            if (Integer.parseInt(num) == selectNum) {
                return i;
            }
            i++;
        }
        return 0;
    }

    /**
     * @author coleman
     */
    private class SettingsAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        private SettingsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.settings_item, null);
            }
            ImageView imgv = (ImageView) convertView.findViewById(R.id.imageView1);
            TextView txtv = (TextView) convertView.findViewById(R.id.textView1);
            HashMap<String, Integer> map = data.get(position);
            imgv.setImageResource(map.get(from[0]));
            txtv.setText(map.get(from[1]));
            return convertView;
        }

    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void dialog(String msg) {
        new AlertDialog.Builder(this).setTitle(R.string.msg_dialog_title).setMessage(msg)
                .setPositiveButton(R.string.ok, null).show();
    }

    private void dialog(int resId) {
        dialog(getString(resId));
    }

    private static class SimpleCrypto {
        private static String KEY = "Coleman";

        public static String encrypt(String seed, String cleartext) throws Exception {
            byte[] rawKey = getRawKey(seed.getBytes());
            byte[] result = encrypt(rawKey, cleartext.getBytes());
            return toHex(result);
        }

        public static String decrypt(String seed, String encrypted) throws Exception {
            byte[] rawKey = getRawKey(seed.getBytes());
            byte[] enc = toByte(encrypted);
            byte[] result = decrypt(rawKey, enc);
            return new String(result);
        }

        private static byte[] getRawKey(byte[] seed) throws Exception {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(seed);
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            return raw;
        }

        private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(clear);
            return encrypted;
        }

        private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return decrypted;
        }

        public static String toHex(String txt) {
            return toHex(txt.getBytes());
        }

        public static String fromHex(String hex) {
            return new String(toByte(hex));
        }

        public static byte[] toByte(String hexString) {
            int len = hexString.length() / 2;
            byte[] result = new byte[len];
            for (int i = 0; i < len; i++)
                result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
            return result;
        }

        public static String toHex(byte[] buf) {
            if (buf == null)
                return "";
            StringBuffer result = new StringBuffer(2 * buf.length);
            for (int i = 0; i < buf.length; i++) {
                appendHex(result, buf[i]);
            }
            return result.toString();
        }

        private final static String HEX = "0123456789ABCDEF";

        private static void appendHex(StringBuffer sb, byte b) {
            sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
        }
    }
}
