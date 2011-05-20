/*
 * Name        : TextEditor.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : TextEditor.java
 * Review      : 
 */

package com.coleman.kingword.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.coleman.kingword.R;
import com.coleman.util.Config;
import com.coleman.util.Log;

public class TextEditor extends Activity implements OnClickListener {
    public static final String ACTION_EDIT_TEXT = "com.coleman.sms.activity.ACTION_EDIT_TEXT";

    public static final String ACTION_EDIT_FILE = "com.coleman.sms.activity.ACTION_EDIT_FILE";

    private static final String TAG = TextEditor.class.getName();

    private EditText editText;

    private Button btnSend;

    private Button btnSplit;

    private String path;

    private IReader binder;

    private ReadFileServiceConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Intent intent = getIntent();
        setContentView(R.layout.text_editor);
        editText = (EditText) findViewById(R.id.editor);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSplit = (Button) findViewById(R.id.btn_split);
        btnSend.setOnClickListener(this);
        btnSplit.setOnClickListener(this);

        if (ACTION_EDIT_TEXT.equals(intent.getAction())) {
            String text = intent.getStringExtra("text");
            Log.d(TAG, "text: " + text);
            editText.setText(text);
        } else if (ACTION_EDIT_FILE.equals(intent.getAction())) {
            path = intent.getStringExtra("path");
            Log.d(TAG, "path: " + path);
            editText.setText("");
            setProgressBarIndeterminateVisibility(true);
            setProgressBarIndeterminate(true);
            doRead(path);
        } else {
            Log.d(TAG, "Unknow action, exiting!");
            finish();
        }
        // gone the split button
        if (!ACTION_EDIT_FILE.equals(getIntent().getAction())) {
            findViewById(R.id.btn_split).setVisibility(View.GONE);
            findViewById(R.id.view_left).setVisibility(View.INVISIBLE);
            findViewById(R.id.view_right).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.editor_option, menu);
        if (!ACTION_EDIT_FILE.equals(getIntent().getAction())) {
            // menu.removeItem(R.id.menu_save);
            menu.removeItem(R.id.menu_split);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                doSend();
                finish();
                break;
            case R.id.menu_split:
                doSplit();
                break;
            case R.id.menu_save:
                String action = getIntent().getAction();
                if (ACTION_EDIT_FILE.equals(action)) {
                    doSave(path, editText.getText().toString());
                } else if (ACTION_EDIT_TEXT.equals(action)) {
                    doSave(editText.getText().toString());
                }
                finish();
                break;
            case R.id.menu_back:
                finish();
                break;
        }
        return true;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                doSend();
                finish();
                break;
            case R.id.btn_split:
                doSplit();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (conn != null) {
            unbindService(conn);
        }
        super.onDestroy();
    }

    private void doRead(String path) {
        Intent intent = new Intent(ColemanIntent.ACTION_FILE_READ);
        intent.putExtra(ColemanIntent.EXTRA_PATH, path);
        conn = new ReadFileServiceConnection();
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    /**
     * save content to database.
     * 
     * @param string
     */
    private void doSave(String string) {
    }

    /**
     * save content to sdcard.
     * 
     * @param path
     * @param text
     */
    private void doSave(String path, final String text) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, R.string.toast_media_unmounted, Toast.LENGTH_SHORT).show();
            return;
        }
        final File file = new File(path);
        if (!file.exists()) {
            return;
        }
        final Thread writeThread = new Thread() {
            public void run() {
                OutputStreamWriter osw = null;
                try {
                    osw = new OutputStreamWriter(new FileOutputStream(file), Config.ENCODE);
                    Log.d(TAG, "fw.getEncoding(): " + osw.getEncoding());
                    System.out.println(text);
                    osw.append(text);
                    osw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        };
        DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        writeThread.start();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.save).setMessage(R.string.save_hint)
                .setPositiveButton(R.string.ok, ocl).setNegativeButton(R.string.cancel, ocl).show();

    }

    private void doSend() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:"));
        String body = editText.getText().toString();
        intent.putExtra("sms_body", body == null ? "" : body);
        startActivity(intent);
    }

    private void doSplit() {
    }

    private void toast(String format, Object... params) {
        if (params != null && params.length > 0) {
            Toast.makeText(this, String.format(format, params), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, format, Toast.LENGTH_SHORT).show();
        }
    }

    private class ReadFileServiceConnection implements ServiceConnection {

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, name + " service disconnected.");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (IReader) service;
            binder.doRead(new Runnable() {
                public void run() {
                    String str = binder.getContent();
                    editText.setText(str);
                    editText.setSelection(0);
                    editText.invalidate();
                    setProgressBarIndeterminateVisibility(false);
                }
            });
        }

    }

    public static interface IReader {
        public void doRead(Runnable callback);

        public String getContent();
    }
}
