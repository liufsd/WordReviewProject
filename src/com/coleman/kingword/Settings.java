
package com.coleman.kingword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.wordlist.WordListAccessor;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.FiniteStateMachine.MultipleState;
import com.coleman.util.Config;
import com.coleman.util.Log;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements OnPreferenceClickListener {
    public static final String RESTORE = "restore";

    public static final String BACKUP = "backup";

    public static final String DATABASE_SET = "database_set";

    public static final String VIEW_METHOD = "view_method";

    protected static final String TAG = Settings.class.getName();

    private Preference prefViewMethod, prefDatabaseSet, prefBackup, prefRestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        prefViewMethod = (Preference) findPreference(VIEW_METHOD);
        prefDatabaseSet = (Preference) findPreference(DATABASE_SET);
        prefBackup = (Preference) findPreference(BACKUP);
        prefRestore = (Preference) findPreference(RESTORE);
        prefViewMethod.setOnPreferenceClickListener(this);
        prefDatabaseSet.setOnPreferenceClickListener(this);
        prefBackup.setOnPreferenceClickListener(this);
        prefRestore.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == prefViewMethod) {
            showViewMethodConfigDialog();
        } else if (preference == prefDatabaseSet) {
            showLanguageDialog();
        } else if (preference == prefBackup) {
            showBackupDialog();
        } else if (preference == prefRestore) {
            showRestoreDialog();
        }
        return false;
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
                        backupDB(Settings.this, true);
                        break;
                    case 1:
                        WordInfoHelper.backupWordInfoDB(Settings.this, true);
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
                        restoreDB(Settings.this, true);
                        break;
                    case 1:
                        WordInfoHelper.restoreWordInfoDB(Settings.this, true);
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

    private void showLanguageDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showSummaryInfoDB();
                        break;
                    case 1:
                        showDetailedInfoDB();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }

        };
        new AlertDialog.Builder(this).setTitle(R.string.language_settings)
                .setItems(R.array.language_settings, listener)
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void showSummaryInfoDB() {
        final Collection<DictLibrary> c = DictManager.getInstance().getLibrarys();
        final String[] names = new String[c.size()];
        int i = 0;
        int index = -1;
        for (DictLibrary dynamicTable : c) {
            names[i] = dynamicTable.getLibDirName();
            if (dynamicTable.isCurLib()) {
                index = i;
            }
            i++;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DictManager.getInstance().setCurLibrary(names[which]);
                dialog.dismiss();
            }

        };
        new AlertDialog.Builder(this).setTitle(R.string.language_settings)
                .setSingleChoiceItems(names, index, listener)
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void showDetailedInfoDB() {
        final Collection<DictLibrary> c = DictManager.getInstance().getLibrarys();
        final String[] names = new String[c.size()];
        int i = 0;
        int index = -1;
        for (DictLibrary dynamicTable : c) {
            names[i] = dynamicTable.getLibDirName();
            if (dynamicTable.isMoreLib()) {
                index = i;
            }
            i++;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DictManager.getInstance().setCurLibrary(names[which]);
                dialog.dismiss();
            }

        };
        new AlertDialog.Builder(this).setTitle(R.string.language_settings)
                .setSingleChoiceItems(names, index, listener)
                .setNegativeButton(R.string.cancel, null).show();
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
        String viewMtd = PreferenceManager.getDefaultSharedPreferences(this).getString(VIEW_METHOD,
                WordListAccessor.DEFAULT_VIEW_METHOD);
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
                                Toast.makeText(Settings.this, R.string.view_method_empty_warn,
                                        Toast.LENGTH_SHORT).show();
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
                        PreferenceManager.getDefaultSharedPreferences(Settings.this).edit()
                                .putString(VIEW_METHOD, rstr).commit();
                        Toast.makeText(Settings.this, R.string.view_method_success_hint,
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
}
