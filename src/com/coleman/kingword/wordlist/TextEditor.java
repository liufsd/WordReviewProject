/*
 * Name        : TextEditor.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : TextEditor.java
 * Review      : 
 */

package com.coleman.kingword.wordlist;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.coleman.kingword.R;
import com.coleman.util.Log;

public class TextEditor extends Activity implements OnClickListener {
    public static final String ACTION_EDIT_FILE = "edit_text";

    private static final String TAG = "TextEditor";

    private EditText editText;

    private String path;

    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.text_editor);
        editText = (EditText) findViewById(R.id.editorText1);
        btnSave = (Button) findViewById(R.id.button1);
        btnSave.setOnClickListener(this);

        if (ACTION_EDIT_FILE.equals(intent.getAction())) {
            path = intent.getStringExtra("path");
            Log.d(TAG, "path: " + path);
            editText.setText("");
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
        // save file to sdcard
        try {
            FileWriter writer = new FileWriter(new File(path));
            writer.write(editText.getText().toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // load the wordlist
        Intent intent = new Intent(this, WordlistTabActivity.class);
        intent.putExtra(WordListActivity.EXTERNAL_FILE, true);
        intent.putExtra(WordListActivity.EXTERNAL_FILE_PATH, path);
        startActivity(intent);
    }

    private class ExpensiveTask extends AsyncTask<Void, Void, Void> {
        private byte type;

        private static final byte LOAD_FILE = 0;

        private String content = "";

        public ExpensiveTask(byte loadFile) {
            this.type = loadFile;
        }

        @Override
        protected void onPreExecute() {
            switch (type) {
                case LOAD_FILE:
                    break;
                default:
                    break;
            }
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
                    ((ProgressBar) findViewById(R.id.progressBar1)).setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    private class FileLoader {

        private String content = "";

        private FileLoader(String path) {
            try {
                FileInputStream fis = new FileInputStream(path);
                int count = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte datas[] = new byte[1024];
                while ((count = fis.read(datas)) > 0) {
                    baos.write(datas, 0, count);
                }
                baos.flush();
                baos.close();
                content = baos.toString();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getContent() {
            return content;
        }

    }
}
