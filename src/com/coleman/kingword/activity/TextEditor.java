/*
 * Name        : TextEditor.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : TextEditor.java
 * Review      : 
 */

package com.coleman.kingword.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
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
    public static final String ACTION_EDIT_FILE = "edit_text";

    private static final String TAG = TextEditor.class.getName();

    private EditText editText;

    private String path;

    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Intent intent = getIntent();
        setContentView(R.layout.text_editor);
        editText = (EditText) findViewById(R.id.editorText1);
        btnSave = (Button) findViewById(R.id.button1);
        btnSave.setOnClickListener(this);

        if (ACTION_EDIT_FILE.equals(intent.getAction())) {
            path = intent.getStringExtra("path");
            Log.d(TAG, "path: " + path);
            editText.setText("");
            setProgressBarIndeterminateVisibility(true);
            setProgressBarIndeterminate(true);
            new ExpensiveTask(ExpensiveTask.LOAD_FILE).execute();
        } else {
            Log.d(TAG, "Unknow action, exiting!");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.editor_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                doSave();
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
            case R.id.button1:
                doSave();
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * save content to sdcard.
     * 
     * @TODO need to do this work in the service.
     * @param path
     * @param text
     */
    private void doSave() {
        
        DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.save).setMessage(R.string.save_hint)
                .setPositiveButton(R.string.ok, ocl).setNegativeButton(R.string.cancel, ocl).show();

    }

    private void toast(String format, Object... params) {
        if (params != null && params.length > 0) {
            Toast.makeText(this, String.format(format, params), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, format, Toast.LENGTH_SHORT).show();
        }
    }

    private class ExpensiveTask extends AsyncTask<Void, Void, Void> {
        private byte type;

        private static final byte LOAD_FILE = 0;

        private String content = "";

        public ExpensiveTask(byte loadFile) {
            this.type = loadFile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (type) {
                case LOAD_FILE:
                    content = new FileLoader(path).getContent();
                    break;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (type) {
                case LOAD_FILE:
                    editText.setText(content);
                    setProgressBarIndeterminate(false);
                    break;
                default:
                    break;
            }
        }
    }

    private class FileLoader {

        private String content;

        private FileLoader(String path) {
            InputStream is = null;
            try {
                is = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            doLoad(is);
        }

        public String getContent() {
            return content;
        }

        private void doLoad(InputStream is) {
            ByteArrayOutputStream baos;
            if (is == null) {
                Log.w(TAG, "InputStream should not be null!");
                return;
            }
            baos = new ByteArrayOutputStream();
            try {
                byte[] buf = new byte[512];
                int count = -1;
                while ((count = is.read(buf)) != -1) {
                    baos.write(buf, 0, count);
                }
                buf = baos.toByteArray();
                Log.d(TAG, buf[0] + "   buf[0]: " + Integer.toHexString(buf[0]));
                Log.d(TAG, buf[1] + "   buf[1]: " + Integer.toHexString(buf[1]));
                content = new String(buf, Config.ENCODE);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e2) {
                }
                try {
                    if (baos != null) {
                        baos.close();
                    }
                } catch (Exception e2) {
                }
            }
        }
    }
}
