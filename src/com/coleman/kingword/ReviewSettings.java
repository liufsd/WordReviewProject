
package com.coleman.kingword;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.TimePicker;

import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.util.Log;

public class ReviewSettings extends PreferenceActivity implements OnPreferenceClickListener {
    protected static final String TAG = ReviewSettings.class.getName();

    private Preference time1, time2, time3;

    private CheckBoxPreference cbpre;

    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.review_settings);
        time1 = findPreference("time1");
        time2 = findPreference("time2");
        time3 = findPreference("time3");
        cbpre = (CheckBoxPreference) findPreference("review");
        time1.setOnPreferenceClickListener(this);
        time2.setOnPreferenceClickListener(this);
        time3.setOnPreferenceClickListener(this);
        cbpre.setOnPreferenceClickListener(this);
        checkDepandency();
        initUI();
    }

    private void initUI() {
        String notSet = getString(R.string.not_set);
        String t1 = getPreferenceScreen().getSharedPreferences().getString("time1", notSet);
        String t2 = getPreferenceScreen().getSharedPreferences().getString("time2", notSet);
        String t3 = getPreferenceScreen().getSharedPreferences().getString("time3", notSet);
        time1.setTitle(t1);
        time2.setTitle(t2);
        time3.setTitle(t3);
        if (notSet.equals(t1)) {
            time1.setSummary("");
        } else {
            time1.setSummary(String.format(getString(R.string.fixed_time_hint), t1));
        }
        if (notSet.equals(t2)) {
            time2.setSummary("");
        } else {
            time2.setSummary(String.format(getString(R.string.fixed_time_hint), t2));
        }
        if (notSet.equals(t3)) {
            time3.setSummary("");
        } else {
            time3.setSummary(String.format(getString(R.string.fixed_time_hint), t3));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == time1) {
            showSetReviewTime(time1, 0, "08:00");
        } else if (preference == time2) {
            showSetReviewTime(time2, 1, "12:00");
        } else if (preference == time3) {
            showSetReviewTime(time3, 2, "20:00");
        } else if (preference == cbpre) {
            checkFixedModeSelect();
        }
        return false;
    }

    private void checkFixedModeSelect() {
        if (cbpre.isChecked()) {
            String notSet = getString(R.string.not_set);
            String t1 = getPreferenceScreen().getSharedPreferences().getString("time1", notSet);
            String t2 = getPreferenceScreen().getSharedPreferences().getString("time2", notSet);
            String t3 = getPreferenceScreen().getSharedPreferences().getString("time3", notSet);
            if (!notSet.equals(t1)) {
                EbbinghausReminder.setRepeatNotifaction(ReviewSettings.this, 0, t1);
            }
            if (!notSet.equals(t2)) {
                EbbinghausReminder.setRepeatNotifaction(ReviewSettings.this, 1, t2);
            }
            if (!notSet.equals(t3)) {
                EbbinghausReminder.setRepeatNotifaction(ReviewSettings.this, 2, t3);
            }
        } else {
            for (int j = 0; j < 3; j++) {
                EbbinghausReminder.removeRepeatNotifaction(ReviewSettings.this, j);
            }
        }
    }

    private void checkDepandency() {
        time1.setDependency("review");
        time2.setDependency("review");
        time3.setDependency("review");
    }

    private void showSetReviewTime(final Preference pre, final int which, String time) {
        OnTimeSetListener callBack = new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                String strTime = format.format(c.getTime());
                pre.setTitle(strTime);
                pre.setSummary(String.format(getString(R.string.fixed_time_hint), strTime));
                getPreferenceScreen().getSharedPreferences().edit()
                        .putString(pre.getKey(), strTime).commit();
                EbbinghausReminder.setRepeatNotifaction(ReviewSettings.this, which, strTime);
                Log.d(TAG, "set new review time: " + strTime);
            }
        };
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        try {
            calendar.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(this, callBack, hourOfDay, minute, true).show();
    }
}
