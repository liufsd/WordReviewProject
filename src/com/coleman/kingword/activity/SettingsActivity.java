
package com.coleman.kingword.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.kingword.wordlist.WordListManager;
import com.coleman.tools.sms.SendManager;
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
        // 2
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_backup);
        map.put(from[1], R.string.backup);
        data.add(map);
        // 3
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_restore);
        map.put(from[1], R.string.restore);
        data.add(map);
        // 4
        map = new HashMap<String, Integer>();
        map.put(from[0], R.drawable.set_level_type);
        map.put(from[1], R.string.learning_level_name_set);
        data.add(map);
        // 5
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
                showConfirmDialog(2);
                break;
            case 3:
                showConfirmDialog(3);
                break;
            case 4:
                showSelectLevelType();
                break;
            case 5:
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
                            String msg = IEMI.matches("0{15}") ? "time:"
                                    + AppSettings.getLong(SettingsActivity.this,
                                            AppSettings.FIRST_STARTED_TIME_KEY, 0) : "IEMI:" + IEMI;
                            msg += "(" + et.getText().toString() + ")";
                            SendManager.sendMessage(SettingsActivity.this, msg);
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
        int saved_pw = AppSettings.getInt(SettingsActivity.this, AppSettings.SAVED_PW_KEY, 0);
        int count_pw = getCountedPw();
        Log.d(TAG, "==================saved pw:" + saved_pw + "  count pw:" + count_pw);
        if (saved_pw == count_pw) {
            return true;
        }
        return false;
    }

    private void checkPwSent(String pw) {
        int count_pw = getCountedPw();
        // Log.d(TAG, "===========================count pw:" + count_pw +
        // "  input pw:" + pw);
        // provide a back door to enter highLevelSettings, use the back door
        // ,you should enter it every time.
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String superpw = "" + (c.get(Calendar.MONTH) + 1 + 10)
                + (c.get(Calendar.DAY_OF_MONTH) + 11);
        if (pw.equals("" + count_pw)) {
            Log.d(TAG, "================password matched!");
            AppSettings.saveInt(SettingsActivity.this, AppSettings.SAVED_PW_KEY, count_pw);
            showHighLevelSettings();

        } else if (pw.equals(superpw)) {
            Log.d(TAG, "================super password matched!");
            AppSettings.saveInt(SettingsActivity.this, AppSettings.SAVED_PW_KEY,
                    Integer.parseInt(superpw));
            showHighLevelSettings();
        } else {

            toast(R.string.pw_failed);
            Log.d(TAG, "================password not matched!");
        }
    }

    private int getCountedPw() {
        String IMEI = Config.getDeviceId(this);
        long pw = AppSettings.getLong(SettingsActivity.this, AppSettings.FIRST_STARTED_TIME_KEY, 0);
        try {
            if (!IMEI.matches("0{15}")) {
                Log.d(TAG, "==================IMEI is not null!");
                // cut the SNR, and compute a password
                pw = Long.parseLong(IMEI.substring(8, 14));
            } else {
                Log.d(TAG, "==================IMEI is null!");
            }
        } catch (Exception e) {
        }
        return (int) (((pw >> 16) & 0xffff) + (pw & 0xffff));
    }

    private int countedPw(boolean isIEMI, String src) {
        String IMEI = src;
        long pw = 0;
        if (!isIEMI) {
            pw = Long.parseLong(src);
        } else {
            try {
                if (!IMEI.matches("0{15}")) {
                    Log.d(TAG, "==================IMEI is not null!");
                    // cut the SNR, and compute a password
                    pw = Long.parseLong(IMEI.substring(8, 14));
                } else {
                    Log.d(TAG, "==================IMEI is null!");
                }
            } catch (Exception e) {
            }
        }
        return (int) (((pw >> 16) & 0xffff) + (pw & 0xffff));
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
                        showRegeistCounted();
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

    private void showRegeistCounted() {
        View layout = LayoutInflater.from(this).inflate(R.layout.regeist_count_fordialog, null);
        final EditText et = (EditText) layout.findViewById(R.id.editText1);
        DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        checkRegeistCode(et.getText().toString());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.regeist_count_title).setView(layout)
                .setPositiveButton(R.string.ok, lis).setNegativeButton(R.string.cancel, lis).show();
    }

    protected void checkRegeistCode(String string) {
        if (string.startsWith("IEMI:")) {
            int pw = countedPw(true, string.substring(5, string.indexOf("(")));
            dialog(String.format(getString(R.string.counted_pw_is), pw));
        } else if (string.startsWith("time:")) {
            int pw = countedPw(false, string.substring(5, string.indexOf("(")));
            dialog(String.format(getString(R.string.counted_pw_is), pw));
        } else {
            dialog(R.string.pw_src_wrong);
        }
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

    private void showConfirmDialog(final int position) {
        String title = "", msg = "";
        switch (position) {
            case 2:
                title = getString(R.string.backup);
                msg = getString(R.string.backup_msg);
                break;
            case 3:
                title = getString(R.string.restore);
                msg = getString(R.string.restore_msg);
                break;
            default:
                break;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        switch (position) {
                            case 2:
                                WordInfoHelper._BACKUP_WHOLE_LIST(SettingsActivity.this);
                                break;
                            case 3:
                                WordInfoHelper._RESTORE_WHOLE_LIST(SettingsActivity.this);
                                break;
                            default:
                                break;
                        }
                        break;

                    default:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.ok, listener)
                .setNegativeButton(R.string.cancel, listener).show();
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
        new AlertDialog.Builder(this).setTitle(R.string.msg_dialog_title).setMessage(resId)
                .setPositiveButton(R.string.ok, null).show();
    }
}
