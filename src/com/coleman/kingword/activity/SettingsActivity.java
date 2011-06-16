
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.util.AppSettings;

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
            default:
                break;
        }
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
                Log.d(TAG, "set split nums: " + nums[which]);
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.set_split_num_hint)
                .setItems(nums, listener).show();
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
}
