/*
 * Name        : FileExplorer.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : FileExplorer.java
 * Review      : 
 */

package com.coleman.kingword.wordlist;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coleman.kingword.R;
import com.coleman.log.Log;
import com.coleman.util.Config;

public class FileExplorer extends Activity {
    public static final String ACTION_EXPLORER = "com.coleman.sms.activity.EXPLORER";

    private static final String TAG = FileExplorer.class.getName();

    private static Log Log = Config.getLog();

    private static final String ROOT_PATH = "/sdcard";

    private ArrayList<File> list = new ArrayList<File>();

    private File root;

    private ListView listView;

    private FileAdapter adapter;

    private TextView pathView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);
        listView = (ListView) findViewById(R.id.listView1);
        pathView = (TextView) findViewById(R.id.textView1);
        root = new File(ROOT_PATH);
        listFiles(list, root);
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "press button...");
                onBackPressed();
            }
        });
        adapter = new FileAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
    }

    private void listFiles(ArrayList<File> list, File dir) {
        list.clear();
        if (dir.isDirectory()) {
            File fs[] = dir.listFiles();
            if (fs != null) {
                Log.d(TAG, "fs.length: " + fs.length);
                list.addAll(Arrays.asList(fs));
            }
        }
        String path = String.format(getString(R.string.path), dir.getPath());
        Log.d(TAG, "path: " + path);
        pathView.setText(path);
    }

    @Override
    public void onBackPressed() {
        if (ROOT_PATH.equals(root.getPath())) {
            super.onBackPressed();
        } else {
            Log.d(TAG, "root.getPath(): " + root.getPath());
            root = root.getParentFile();
            listFiles(list, root);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // MenuInflater inflater = new MenuInflater(this);
        // inflater.inflate(R.menu.explorer_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch (item.getItemId()) {
        // case R.id.menu_back:
        // finish();
        // break;
        // }
        return true;
    }

    private class FileAdapter extends BaseAdapter implements OnItemClickListener {
        private LayoutInflater inflater;

        private ArrayList<File> list;

        public FileAdapter(Context context, ArrayList<File> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        public int getCount() {
            return list.size();
        }

        public File getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.file_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            String fileName = list.get(position).getName();
            textView.setText(fileName);
            convertView.setTag(fileName);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView1);
            if (!TextUtils.isEmpty(fileName)) {
                if (list.get(position).isDirectory()) {
                    String fs[] = list.get(position).list();
                    if (fs != null && fs.length > 0) {
                        imgView.setImageResource(R.drawable.folder);
                    } else {
                        imgView.setImageResource(R.drawable.folder_emp);
                    }
                } else if (fileName.toLowerCase().endsWith(".txt")) {
                    imgView.setImageResource(R.drawable.txt_file);
                } else {
                    imgView.setImageResource(R.drawable.unknow_file);
                }
            } else {
                imgView.setImageResource(R.drawable.unknow_file);
            }
            return convertView;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String fn = String.valueOf(view.getTag());
            root = getItem(position);
            if (root.isDirectory()) {
                listFiles(list, root);
                notifyDataSetChanged();
            } else {
                if (fn != null && fn.toLowerCase().indexOf(".txt") != -1) {
                    doEdit(getItem(position).getPath());
                } else {
                    Toast.makeText(FileExplorer.this, getString(R.string.file_not_txt),
                            Toast.LENGTH_SHORT).show();
                }
                root = root.getParentFile();
            }

        }
    }

    private void doEdit(final String path) {
        if (!Config.isExternalMediaMounted()) {
            Toast.makeText(this, R.string.toast_media_unmounted, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(TextEditor.ACTION_EDIT_FILE);
        intent.putExtra("path", path);
        startActivity(intent);
        getParent().finish();
    }
}
